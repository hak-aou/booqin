package fr.uge.booqin.app.service.loan;

import fr.uge.booqin.app.dto.book.BookWaitListStatusDTO;
import fr.uge.booqin.app.dto.book.LendBookRequest;
import fr.uge.booqin.app.dto.book.UpdateLendBook;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.app.service.TransactionService;
import fr.uge.booqin.app.service.loan.bookstock.BookLock;
import fr.uge.booqin.app.service.loan.bookstock.BookStockManager;
import fr.uge.booqin.app.service.observer.obs_interface.BookStockObserver;
import fr.uge.booqin.app.service.observer.obs_interface.LoanObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.persistence.entity.loan.BookSupplyAndDemandEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.loan.BookSupplyAndDemandRepository;
import fr.uge.booqin.infra.persistence.repository.loan.WaitingListEntryEntityRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Stream;

@Service
public class BookLoanService implements BookStockObserver, ApplicationListener<ContextRefreshedEvent> {

    private final Logger logger = Logger.getLogger(BookLoanService.class.getName());
    private final Validator validator;
    private final UserRepository userRepository;
    private final BookSupplyAndDemandRepository bookSupplyAndDemandRepository;
    private final WaitingListEntryEntityRepository waitingListEntryEntityRepository;
    private final CartService cartService;
    private final TransactionService transactionService;
    private final BookStockManager bookStockManager;
    private final BooqInConfig booqInConfig;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);

    private final HashMap<UUID, Set<BookLoanService.PropositionLockRecord>> bookIndexedPropositionLocks = new HashMap<>();
    private final HashMap<UUID, Set<BookLoanService.PropositionLockRecord>> userIndexedPropositionLocks = new HashMap<>();
    private final Map<UUID, PropositionLockRecord> tokenIndexedPropositionLocks = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final List<LoanObserver> observers;


    public record PropositionLockRecord(User user, UUID bookId, BookLock lock, ScheduledFuture<?> scheduledCallback) {
    }

    public BookLoanService(
            UserRepository userRepository,
            WaitingListEntryEntityRepository waitingListEntryEntityRepository,
            BookSupplyAndDemandRepository bookSupplyAndDemandRepository,
            BookStockManager bookStockManager,
            BooqInConfig booqInConfig,
            Validator validator,
            List<LoanObserver> observers,
            CartService cartService,
            TransactionService transactionService
            ) {
        this.userRepository = userRepository;
        this.bookSupplyAndDemandRepository = bookSupplyAndDemandRepository;
        this.waitingListEntryEntityRepository = waitingListEntryEntityRepository;
        this.booqInConfig = booqInConfig;
        this.bookStockManager = bookStockManager;
        this.cartService = cartService;
        this.validator = validator;
        this.transactionService = transactionService;
        this.observers = new ArrayList<>(observers);
        bookStockManager.subscribe(this);
    }

    @Transactional
    public boolean canUserLendBook(UUID userId, UUID bookId) {

        var isInWaitlist = bookSupplyAndDemandRepository.findByBookAndUserIdInWaitingQueue(bookId, userId)
                .isPresent();
        return !isInWaitlist;
    }

    @Transactional
    public void lendBook(User user, LendBookRequest request) {
        ServiceUtils.checkRequest(validator, request);
        if (!canUserLendBook(user.id(), request.bookId())) {
            throw new TheirFaultException("User can't lend book he is waiting for");
        }
        bookStockManager.updateOrRemove(user, new UpdateLendBook(request.bookId(), request.quantity()));
        // should be async but I can't test it
        processLendingEvent(request.bookId());
    }

    @Transactional
    public void processLendingEvent(UUID bookId) {
        lock.lock();
        try {
            var law = bookSupplyAndDemandRepository.findByBookId(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("book offers not found"));
            processLendingEvent(law);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void processLendingEvent(BookSupplyAndDemandEntity law) {
        lock.lock();
        var book = law.getBook();
        try {
            logger.info("Processing lending event for book " + book.getId());
            if (law.getDemand() > 0) {

                // very important that the waitingQueue is LAZY-select with no BATCH
                // in order to have the very current user on top of the list
                BookLock bookLock;
                while ((bookLock = bookStockManager.lockBook(book.getId(), booqInConfig.borrowWaitingListLockDuration())) != null) {
                    var waitingEntry = law.pollWaitingUser();
                    if (waitingEntry == null) {
                        bookStockManager.releaseBook(bookLock);
                        break;
                    }
                    var userEntity = waitingEntry.getUser();
                    logger.info("Proposition of book " + book.getId() + " to user " + userEntity.getId());
                    // send a notification to the user (async)
                    var expirationCallback = scheduler.schedule(() -> {
                        expireReservation(waitingEntry.getUser().getId(), book.getId());
                        logger.info("Reservation expired for user " + userEntity.getId() + " on book " + book.getId());
                    }, booqInConfig.borrowWaitingListLockDuration().toSeconds(), TimeUnit.SECONDS);
                    var proposition = new BookLoanService.PropositionLockRecord(UserMapper.from(userEntity), bookLock.bookId(), bookLock, expirationCallback);
                    bookIndexedPropositionLocks.computeIfAbsent(book.getId(), k -> new HashSet<>())
                            .add(proposition);
                    userIndexedPropositionLocks.computeIfAbsent(userEntity.getId(), k -> new HashSet<>())
                            .add(proposition);
                    tokenIndexedPropositionLocks.put(bookLock.token(), proposition);
                    observers.forEach(observer -> observer.propositionEvent(UserMapper.from(userEntity), book.getTitle()));
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    @Transactional
    public void bookAvailable(BookInfo bookInfo) {
        processLendingEvent(bookInfo.bookId());
    }

    @Override
    @Transactional
    public void expiredLockToken(UUID bookId, UUID token) {
        // check if the lock exists
        lock.lock();
        try {
            var proposition = tokenIndexedPropositionLocks.remove(token);
            if (proposition != null) {
                bookIndexedPropositionLocks.computeIfPresent(bookId, (k, v) -> {
                    v.remove(proposition);
                    return v;
                });
                userIndexedPropositionLocks.computeIfPresent(proposition.user().id(), (k, v) -> {
                    v.remove(proposition);
                    return v;
                });

                var maybeUser = userRepository.findById(proposition.user().id());
                if (maybeUser.isPresent()) {
                    var user = UserMapper.from(maybeUser.get());
                    observers.forEach(observer -> observer.propositionExpiredCausedBySupplyDrop(user, proposition.lock().bookId().toString()));
                    borrowBookWithStrategy(user, bookId, BookSupplyAndDemandEntity::addOnTopOfWaitingQueue);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void expireReservation(UUID userId, UUID bookId) {
        removeLock(userId, bookId);
    }

    private void removeLock(UUID userId, UUID bookId) {
        lock.lock();
        try {
            // remove the lock
            userIndexedPropositionLocks.getOrDefault(userId, Set.of()).stream()
                    .filter(getPropositionLockRecordPredicate(bookId))
                    .findFirst()
                    .ifPresent(record -> {
                        bookIndexedPropositionLocks.computeIfPresent(bookId, (k, v) -> {
                            v.remove(record);
                            return v;
                        });
                        userIndexedPropositionLocks.computeIfPresent(userId, (k, v) -> {
                            v.remove(record);
                            return v;
                        });
                        tokenIndexedPropositionLocks.remove(record.lock().token());
                        record.scheduledCallback().cancel(false);
                    });
        } finally {
            lock.unlock();
        }
    }

    private static Predicate<PropositionLockRecord> getPropositionLockRecordPredicate(UUID bookId) {
        return record -> record.lock().bookId().equals(bookId);
    }

    public Set<BookLoanService.PropositionLockRecord> userIndexedPropositionLocks(UUID userId) {
        lock.lock();
        try {
            return userIndexedPropositionLocks.getOrDefault(userId, Set.of());
        } finally {
            lock.unlock();
        }
    }

    public Optional<PropositionLockRecord> removeAndGetLockOnBook(UUID userId, UUID bookId) {
        lock.lock();
        try {
            logger.info("Removing lock for user " + userId + " on book " + bookId);
            var lock = userIndexedPropositionLocks.getOrDefault(userId, Set.of()).stream()
                    .filter(getPropositionLockRecordPredicate(bookId))
                    .findFirst();
            removeLock(userId, bookId);
            return lock;
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void borrowBook(User from, UUID bookId) {
        // classic strategy (append to the waiting list)
        borrowBookWithStrategy(from, bookId, BookSupplyAndDemandEntity::appendWaitingUser);
    }

    public void borrowBookWithStrategy(User from, UUID bookId, BiConsumer<BookSupplyAndDemandEntity, UserEntity> strategy) {
        var userEntity = userRepository.findById(from.id())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        var offers = bookSupplyAndDemandRepository.findByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("book offers not found"));
        // if book is available we add it to the cart
        if (bookStockManager.supply(bookId) > 0) {
            cartService.addBookToCart(from, bookId);
            return;
        }
        // if book is not available we add the user to the waiting list
        strategy.accept(offers, userEntity);
        bookSupplyAndDemandRepository.save(offers);
        logger.info("User " + from.id() + " added to queue for book " + bookId);
        observers.forEach(observer -> observer.userAddedInWaitList(from, offers.getBook().getTitle()));
    }

    @Transactional
    public void unBorrowBook(User from, UUID bookId) {
        var bookSupply = bookSupplyAndDemandRepository.findByBookId(bookId)
                .orElseThrow(() -> new TheirFaultException("book not found"));
        var deleted = waitingListEntryEntityRepository.findByUserIdAndBookId(from.id(), bookId);
        if (deleted.isPresent()) {
            bookSupply.decrementDemand();
            bookSupplyAndDemandRepository.save(bookSupply);
            waitingListEntryEntityRepository.delete(deleted.get());
        }
        expireReservation(from.id(), bookId);
        logger.info("User " + from.id() + " removed from queue for book " + bookId);
    }

    @Transactional
    public BookExchangeInfo getBookSupplyAndDemand(User user, UUID bookId) {
        var offers = bookSupplyAndDemandRepository.findByBookId(bookId)
                .orElseThrow(() -> new IllegalArgumentException("book offers not found"));


        return new BookExchangeInfo(
                bookId,
                bookStockManager.supply(bookId),
                offers.getDemand(),
                userIndexedPropositionLocks.getOrDefault(user.id(), Set.of())
                        .stream()
                        .anyMatch(getPropositionLockRecordPredicate(bookId)) // user has a proposition
                        || bookSupplyAndDemandRepository.findByBookAndUserIdInWaitingQueue(bookId, user.id()).isPresent() // user is in waitlist
                ,
                bookStockManager.isUserLendingBook(user.id(), bookId),
                cartService.isBookInCart(user, bookId),
                transactionService.findTxByBook(user.id(), bookId).orElse(null)
        );
    }

    /// Get all book the user wishes to borrow
    /// with the status of the book (locked or not) and the time it was proposed to the user
    @Transactional
    public List<BookWaitListStatusDTO> getWaitlistStatus(User user) {
        //
        lock.lock();
        try {
            var myBorrowings = waitingListEntryEntityRepository.findAllByUserIdInWaitingQueue(user.id()).stream()
                    .map(entry -> {
                        var book = entry.getBookLoanOffersEntity().getBook();
                        return new BookWaitListStatusDTO(
                                book.getId(),
                                book.getTitle(),
                                book.getImageLinks(),
                                false,
                                Instant.now()
                        );
                    });
            var myPropositions = userIndexedPropositionLocks(user.id());
            return Stream.concat(myBorrowings,
                            myPropositions.stream().map(BookWaitListStatusDTO::from))
                    .toList();
        } finally {
            lock.unlock();
        }
    }

    /// When a user accept a proposition after a book is proposed to him
    @Transactional
    public void acceptBook(UUID bookId, User user) {
        // check if the lock exists
        var proposition = removeAndGetLockOnBook(user.id(), bookId).
                orElseThrow(() -> new TheirFaultException("User does not have a lock on the book"));
        proposition.scheduledCallback().cancel(false);
        if (!bookStockManager.checkToken(bookId, proposition.lock().token())) {
            // @Todo:
            //  observers.forEach(observer -> observer.notifyBookUnavailable(user, proposition.lock().bookInfo().title()));
            return;
        }
        cartService.addBookToCartWithLock(user, proposition.lock());
    }

    @Transactional
    public void loadWaitlists() {
        var supply = bookSupplyAndDemandRepository.findAllWithwaitingQueueNotEmpty();
        for (var bookSupply : supply) {
            processLendingEvent(bookSupply);
        }
    }

    @Override
    @Transactional
    public void onApplicationEvent(@NotNull ContextRefreshedEvent event) {
        loadWaitlists();
    }
}
