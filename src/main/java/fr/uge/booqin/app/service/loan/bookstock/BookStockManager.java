package fr.uge.booqin.app.service.loan.bookstock;


import fr.uge.booqin.app.dto.book.UpdateLendBook;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.loan.BookInfo;
import fr.uge.booqin.app.service.observer.obs_interface.BookStockObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.books.ImageFormatModel;
import fr.uge.booqin.infra.persistence.entity.loan.LoanOfferEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.loan.LoanOfferRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
public class BookStockManager {
    private final Logger logger = Logger.getLogger(BookStockManager.class.getName());

    private final BookRepository bookRepository;
    private final LoanOfferRepository loanOfferRepository;
    private final UserRepository userRepository;
    private final List<BookStockObserver> bookStockObservers = new ArrayList<>();

    private final HashMap<UUID, BookSupplyRecord> availability = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    public BookStockManager(
            BookRepository bookRepository,
            LoanOfferRepository loanOfferRepository,
            UserRepository userRepository
    ) {
        this.bookRepository = bookRepository;
        this.loanOfferRepository = loanOfferRepository;
        this.userRepository = userRepository;
    }

    public void subscribe(BookStockObserver observer) {
        bookStockObservers.add(observer);
    }

    @Transactional
    public Optional<UserEntity> getBookForExchange(UUID bookId, Optional<UUID> token) {
        lock.lock();
        try {
            // @Todo: Change that, to find user that borrowed the most
            // possible amelioration: find the user that has the most books (pass a list of books)
            // for now, get any offer
            var bookLoanOffer = loanOfferRepository.findByBookId(bookId, LockModeType.PESSIMISTIC_WRITE);
            if(bookLoanOffer.isEmpty()) {
                return Optional.empty();
            }
            var owner = bookLoanOffer.get().getUser();
            if(removeOne(UserMapper.from(owner), bookId, token)){
                return Optional.of(owner);
            }
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    public void releaseBook(BookLock bookLock) {
        lock.lock();
        try {
            var lockRecord = availability.get(bookLock.bookId());
            if (lockRecord != null) {
                lockRecord.releaseLock(bookLock);
            }
            for (var observer : bookStockObservers) {
                observer.bookAvailable(bookLock.bookInfo());
            }
            logger.info("Book " + bookLock.bookId() + " released");
        } finally {
            lock.unlock();
        }
    }

    public BookLock lockBook(UUID bookId, Duration duration) {
        lock.lock();
        try {
            var lockRecord = availability.get(bookId);
            if (lockRecord != null) {
                return lockRecord.acquireLockAndGetToken(duration);
            }
            return null;
        } finally {
            lock.unlock();
        }
    }

    public Optional<BookLock> renewLock(BookLock bookLock, Duration duration) {
        lock.lock();
        try {
            var lockRecord = availability.get(bookLock.bookId());
            if (lockRecord != null && lockRecord.hasToken(bookLock.token())) {
                return Optional.of(new BookLock(bookLock.bookInfo(), Instant.now().plus(duration), bookLock.token()));
            }
            return Optional.empty();
        } finally {
            lock.unlock();
        }
    }

    public boolean checkToken(UUID bookId, UUID token) {
        lock.lock();
        try {
            var record = availability.get(bookId);
            return record != null && record.hasToken(token);
        } finally {
            lock.unlock();
        }
    }

    private void addBook(User owner, UUID bookId, int count) {
        lock.lock();
        try {
            if(count < 1) {
                throw new IllegalArgumentException("Count must be greater than 0");
            }
            var userEntity = userRepository.findById(owner.id())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            var book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new IllegalArgumentException("Book not found"));
            var bookLoanOffer = new LoanOfferEntity(userEntity, book, count);
            loanOfferRepository.save(bookLoanOffer);
            // todo manage if offer already exists => then increase quantity
            availability.compute(bookId, (k, v) -> {
                if(v == null) {
                    return new BookSupplyRecord(count, new BookInfo(bookId, book.getTitle(), book.getImageLinks()));
                }
                v.addSupply(count);
                return v;
            });

        } finally {
            lock.unlock();
        }
    }

    private void updateBookOffer(User owner, UUID bookId, int quantity, boolean overwrite) {
        lock.lock();
        try {
            var maybeBookLoanOffer = loanOfferRepository.findByBookIdAndOwnerId(owner.id(), bookId, LockModeType.PESSIMISTIC_WRITE);
            if (maybeBookLoanOffer.isEmpty()) {
                addBook(owner, bookId, quantity);
                return;
            }
            var bookLoanOffer = maybeBookLoanOffer.get();
            var currentQuantity = bookLoanOffer.getQuantity();
            var newQuantity = overwrite ? quantity : currentQuantity + quantity;
            if (newQuantity <= 0) {
                loanOfferRepository.delete(bookLoanOffer);
            } else {
                bookLoanOffer.setQuantity(newQuantity);
                loanOfferRepository.save(bookLoanOffer);
            }
            updateAvailability(bookId, newQuantity - currentQuantity);
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void incrDecrOffer(User owner, UpdateLendBook request) {
        updateBookOffer(owner, request.bookId(), request.quantity(), false);
    }

    @Transactional
    public void updateOrRemove(User owner, UpdateLendBook update) {
        if (update.quantity() < 0) {
            throw new IllegalArgumentException("Count must be strictly positive");
        }
        updateBookOffer(owner, update.bookId(), update.quantity(), true);
    }

    private void updateAvailability(UUID bookId, int diff) {
        lock.lock();
        try {
            availability.compute(bookId, (k, v) -> {
                if(v == null) {
                    logger.log(Level.SEVERE, "BookSupplyRecord should exists");
                    return v;
                }
                if(diff < 0) {
                    var tokensInvalidatedByDropsOfSupply = v.removeSupply(-diff);
                    // notify observers that some tokens are invalidated
                    for (var observer : bookStockObservers) {
                        for (var tokenInvalidated : tokensInvalidatedByDropsOfSupply) {
                            observer.expiredLockToken(bookId, tokenInvalidated);
                        }
                    }
                } else {
                    v.addSupply(diff);
                }
                return v;
            });
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public boolean removeOne(User owner, UUID bookId, Optional<UUID> token) {
        lock.lock();
        try {
            var maybeBookLoanOffer = loanOfferRepository.findByBookIdAndOwnerId(owner.id(), bookId, LockModeType.PESSIMISTIC_WRITE);
            if(maybeBookLoanOffer.isEmpty()) {
                return false;
            }
            var offer = maybeBookLoanOffer.get();
            var quantity = offer.getQuantity() - 1;
            if(quantity == 0) {
                loanOfferRepository.delete(offer);
            } else {
                offer.setQuantity(quantity);
                loanOfferRepository.save(offer);
            }
            var lockRecord = availability.get(bookId);
            if(lockRecord != null) {
                if(token.isPresent()) {
                    lockRecord.removeSupply(Set.of(token.get()));
                } else {
                    var tokensInvalidatedByDropsOfSupply = lockRecord.removeSupply(1);
                    // notify observers that
                    for (var observer : bookStockObservers) {
                        for (var tokenInvalidated : tokensInvalidatedByDropsOfSupply) {
                            observer.expiredLockToken(bookId, tokenInvalidated);
                        }
                    }
                }
            }
            return true;
        } finally {
            lock.unlock();
        }
    }

    public int supply(UUID bookId) {
        lock.lock();
        try {
            var record = availability.get(bookId);
            return record == null ? 0 : record.count();
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public boolean isUserLendingBook(UUID userId, UUID bookId) {
        lock.lock();
        try {
            // not critic, no need to lock
            return loanOfferRepository.findByBookIdAndOwnerId(userId, bookId, LockModeType.NONE)
                    .map(loanOfferEntity -> loanOfferEntity.getUser().getId().equals(userId))
                    .orElse(false);
        } finally {
            lock.unlock();
        }
    }

    public record LoanOfferDTO(UUID bookId, String title, ImageFormatModel images, int quantity) {}

    @Transactional
    public List<LoanOfferDTO> myOffers(User user) {
        lock.lock();
        try {
            var offers = loanOfferRepository.findByOwnerId(user.id());
            var result = new ArrayList<LoanOfferDTO>();
            for (var offer : offers) {
                // TODO: Fix this, bad design
                // BookService depends on bookStockManager, so circular dependency
                // bookstockmanager shoulnd't return a DTO and be used by a controller
                // its the submerged part of the iceberg, and shouldn't be visible outside the service layer
                var bookInfo = BookService.from(this, offer.getBook());
                result.add(new LoanOfferDTO(offer.getBook().getId(), bookInfo.title(),
                        bookInfo.imageLinks(), offer.getQuantity())
                        );
            }
            return result;
        } finally {
            lock.unlock();
        }
    }

    @PostConstruct
    public void init() {
        // Initialize the availability of books
        var offers = loanOfferRepository.findAll();
        for (var offer : offers) {
            availability.compute(offer.getBook().getId(), (k, v) -> {
                if(v == null) {
                    var book = offer.getBook();
                    var bookInfo = new BookInfo(book.getId(), book.getTitle(), book.getImageLinks());
                    return new BookSupplyRecord(offer.getQuantity(), bookInfo);
                }
                v.addSupply(offer.getQuantity());
                return v;
            });
        }
    }


}
