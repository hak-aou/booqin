package fr.uge.booqin.app.service.loan;

import fr.uge.booqin.app.dto.book.UpdateLendBook;
import fr.uge.booqin.app.dto.cart.*;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.TransactionService;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.loan.bookstock.BookLock;
import fr.uge.booqin.app.service.loan.bookstock.BookStockManager;
import fr.uge.booqin.app.service.observer.obs_interface.BookStockObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.cart.OrderStatus;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.cart.BookTransactionEntity;
import fr.uge.booqin.infra.persistence.entity.cart.CartEntity;
import fr.uge.booqin.infra.persistence.entity.cart.OrderEntity;
import fr.uge.booqin.infra.persistence.entity.cart.TransactionStepEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.cart.CartRepository;
import fr.uge.booqin.infra.persistence.repository.cart.OrderRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.logging.Logger;

@Service
public class CartService implements BookStockObserver {

    record securedBook(BookEntity book, UserEntity lender) { }
    public record CartLockedBook(UUID bookId, BookLock bookLock,  ScheduledFuture<?> scheduledCallback) { }

    private final Logger logger = Logger.getLogger(CartService.class.getName());
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private final UserRepository userRepository;

    private final HashMap<UUID, List<CartLockedBook>> bookIndexedUserLocks = new HashMap<>();
    private final HashMap<UUID, List<CartLockedBook>> userIndexedBookLocks = new HashMap<>();
    private final ReentrantLock lock = new ReentrantLock();

    private final BooqInConfig booqInConfig;
    private final BookStockManager bookStockManager;
    private final CartRepository cartRepository;
    private final BookRepository bookRepository;
    private final BookService bookService;
    private final OrderRepository orderRepository;
    private final TransactionService transactionService;

    public CartService(BooqInConfig booqInConfig,
                       BookStockManager bookStockManager,
                       CartRepository cartRepository,
                       BookRepository bookRepository,
                       BookService bookService, UserRepository userRepository,
                       OrderRepository orderRepository,
                          TransactionService transactionService
    ) {
        this.booqInConfig = booqInConfig;
        this.bookStockManager = bookStockManager;
        this.cartRepository = cartRepository;
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.transactionService = transactionService;
    }

    public boolean isBookInCart(User user, UUID bookId) {
        lock.lock();
        try {
            var cart = cartRepository.findByOwnerIdWithBookId(user.id(), bookId, LockModeType.PESSIMISTIC_READ);
            return cart.isPresent();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void expiredLockToken(UUID bookId, UUID token) {
        lock.lock();
        try {
            // @Todo improve by using tokenIndexedLocks
            var maybeCartLock = bookIndexedUserLocks.getOrDefault(bookId, List.of()).stream()
                    .filter(cartLock -> cartLock.bookLock().token().equals(token))
                    .findFirst();
            if (maybeCartLock.isPresent()) {
                var cartLock = maybeCartLock.get();
                cartLock.scheduledCallback().cancel(false);
                bookStockManager.releaseBook(cartLock.bookLock());
            }
            bookIndexedUserLocks.computeIfPresent(bookId, (k, v) -> {
                v.removeIf(cartLock -> cartLock.bookLock().token().equals(token));
                return v;
            });
            userIndexedBookLocks.values().forEach(lockedBooks -> lockedBooks.removeIf(cartLock -> cartLock.bookLock().token().equals(token)));
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void addBookToCart(User user, UUID bookId) {
        lock.lock();
        try {
            // store the book in the basket in db
            var cart = cartRepository.findByOwnerId(user.id(), LockModeType.PESSIMISTIC_WRITE)
                    .orElseThrow(() -> new OurFaultException("User does not have a cart"));
            var book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new TheirFaultException("Book does not exist"));
            cart.addBook(book);
            var priceEstimation = booqInConfig.priceEstimator().estimatePrice(cart.getBooks().size());
            cart.setEstimatedPrice(priceEstimation);
            cartRepository.save(cart);
            logger.info("Book `" + book.getTitle() + "` added to basket of user `" + user.username());
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void addBookToCartWithLock(User user, BookLock bookLock) {
        lock.lock();
        try {
            var lockDuration = booqInConfig.basketLockTimeout();
            var maybeRenewedBookLock = bookStockManager.renewLock(bookLock, lockDuration);
            if (maybeRenewedBookLock.isEmpty()) {
                // @Todo: send a notification
                return;
            }
            var renewedBookLock = maybeRenewedBookLock.get();
            var expiredLockcallBack = scheduler.schedule(() -> removeBookLockFromCart(user, bookLock.bookId()), lockDuration.toSeconds(), TimeUnit.SECONDS);
            var lockedBook = new CartLockedBook(bookLock.bookId(), renewedBookLock, expiredLockcallBack);
            // store the lock
            bookIndexedUserLocks.computeIfAbsent(bookLock.bookId(), k -> new ArrayList<>()).add(lockedBook);
            userIndexedBookLocks.computeIfAbsent(user.id(), k -> new ArrayList<>()).add(lockedBook);
            addBookToCart(user, bookLock.bookId());
            logger.info(user.username() + " has lock on " + lockedBook.bookLock().bookInfo().title() + " for " + lockDuration.toMinutes() + " minutes");
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void removeBookFromCart(User user, UUID bookId) {
        lock.lock();
        try {
            // release the lock if it exists
            removeBookLockFromCart(user, bookId);
            // remove the book from the basket in db
            var cart = cartRepository.findByOwnerId(user.id(), LockModeType.PESSIMISTIC_WRITE)
                    .orElseThrow(() -> new OurFaultException("User does not have a cart"));
            var book = bookRepository.findById(bookId)
                    .orElseThrow(() -> new TheirFaultException("Book does not exist"));
            cart.removeBook(book);
            var priceEstimation = booqInConfig.priceEstimator().estimatePrice(cart.getBooks().size());
            cart.setEstimatedPrice(priceEstimation);
            cartRepository.save(cart);
            logger.info("Book `" + bookId + "` removed from basket of user `" + user.username());
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public void removeBookLockFromCart(User user, UUID bookId) {
        lock.lock();
        try {
            var maybeCartLock = bookIndexedUserLocks.getOrDefault(bookId, List.of()).stream()
                    .filter(cartLock -> cartLock.bookLock().bookId().equals(bookId))
                    .findFirst();
            // don't don functional
            if (maybeCartLock.isPresent()) {
                var cartLock = maybeCartLock.get();
                cartLock.scheduledCallback().cancel(false);
                bookStockManager.releaseBook(cartLock.bookLock());
            }
            // remove the lock
            bookIndexedUserLocks.computeIfPresent(bookId, (k, v) -> {
                v.removeIf(cartLock -> cartLock.bookLock().bookId().equals(bookId));
                return v;
            });
            userIndexedBookLocks.computeIfPresent(user.id(), (k, v) -> {
                v.removeIf(cartLock -> cartLock.bookLock().bookId().equals(bookId));
                return v;
            });
            logger.info("Lock removed for book `" + bookId + "` for user `" + user.username() + "`");
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public Optional<Double> getOrderAmount(User user, UUID orderId, Long cartVersion) {
        lock.lock();
        try {
            var order = orderRepository.findById(orderId, LockModeType.PESSIMISTIC_READ)
                    .orElseThrow(() -> new TheirFaultException("Order does not exist"));
            if(order.getStatus() != OrderStatus.PENDING) {
                return Optional.empty();
            }
            if (!order.getUser().getId().equals(user.id())) {
                throw new TheirFaultException("This order does not exists");
            }
            if (!order.getCartVersion().equals(cartVersion)) {
                return Optional.empty();
            }
            return Optional.of(order.getAmount());
        } finally {
            lock.unlock();
        }
    }

    @Transactional
    public CartDTO getCart(User user) {
        lock.lock();
        try {
            var cart = cartRepository.findByOwnerId(user.id(), LockModeType.PESSIMISTIC_READ)
                    .orElseThrow(() -> new OurFaultException("User does not have a cart"));
            var books = cart.getBooks().stream()
                    .map(book -> {
                        var lockedBook = bookIndexedUserLocks.getOrDefault(book.getId(), List.of()).stream()
                                .filter(cartLock -> cartLock.bookLock().bookId().equals(book.getId()))
                                .findFirst();
                        return new LockedBookDTO(
                                bookService.from(book),
                                lockedBook.isPresent(),
                                lockedBook.map(cartLock -> cartLock.bookLock().expirationTime()).orElse(null));
                    })
                    .toList();
            return new CartDTO(user.id(), books, cart.getVersion(), cart.getEstimatedPrice());
        } finally {
            lock.unlock();
        }
    }


    @Transactional
    public CheckoutDTO checkout(User user, Long cartVersion) {
        lock.lock();
        try {
            var cartOwner = userRepository.findById(user.id())
                    .orElseThrow(() -> new TheirFaultException("User does not exist"));
            var cart = cartRepository.findByOwnerId(user.id(), LockModeType.PESSIMISTIC_WRITE)
                    .orElseThrow(() -> new OurFaultException("User does not have a cart"));

            if (!cart.getVersion().equals(cartVersion)) {
                return new CheckoutDTO(true, "Your cart has changed", null);
            }

            var previousOrder = handlePreviousOrder(user, cartVersion);
            if (previousOrder != null) {
                return previousOrder;
            }

            var securedBooks = secureBooks(user, cart.getBooks().stream().toList());
            if (securedBooks == null) {
                return new CheckoutDTO(true, "Some books are not available anymore", null);
            }

            var transactions = createTransactions(securedBooks);
            var amount = calculateTotalAmount(transactions);

            var order = createAndSaveOrder(cartOwner, cart, transactions, amount);
            return new CheckoutDTO(false, null, transactionService.from(order));
        } finally {
            lock.unlock();
        }
    }

    private CheckoutDTO handlePreviousOrder(User user, Long cartVersion) {
        var maybePreviousOrder = orderRepository.findByUserIdAndStatus(user.id(), OrderStatus.PENDING);
        if (!maybePreviousOrder.isEmpty()) {
            var order = maybePreviousOrder.getFirst();
            if (order.getCartVersion().equals(cartVersion)) {
                return new CheckoutDTO(false, null, transactionService.from(order));
            } else {
                cancelOrder(order);
            }
        }
        return null;
    }

    private void cancelOrder(OrderEntity order) {
        order.setStatus(OrderStatus.CANCELLED);
        orderRepository.save(order);
        for (var transaction : order.getBookTransactions()) {
            for (var book : transaction.getBooks()) {
                bookStockManager.incrDecrOffer(UserMapper.from(transaction.getBookOwner()), new UpdateLendBook(book.getId(), 1));
            }
        }
    }

    private List<securedBook> secureBooks(User user, List<BookEntity> books) {
        var securedBooks = new ArrayList<securedBook>();
        for (var book : books) {
            var maybeBookLockToken = userIndexedBookLocks.getOrDefault(user.id(), List.of()).stream()
                    .filter(cartLock -> cartLock.bookLock().bookId().equals(book.getId()))
                    .map(cartLock -> cartLock.bookLock().token())
                    .findFirst();
            var lender = bookStockManager.getBookForExchange(book.getId(), maybeBookLockToken);
            if (lender.isEmpty()) {
                rollbackSecuredBooks(securedBooks);
                return null;
            }
            securedBooks.add(new securedBook(book, lender.get()));
        }
        return securedBooks;
    }

    private void rollbackSecuredBooks(List<securedBook> securedBooks) {
        for (var securedBook : securedBooks) {
            bookStockManager.incrDecrOffer(UserMapper.from(securedBook.lender), new UpdateLendBook(securedBook.book().getId(), 1));
        }
    }

    private List<BookTransactionEntity> createTransactions(List<securedBook> securedBooks) {
        var ownersById = new HashMap<UUID, UserEntity>();
        var bookByOwners = new HashMap<UUID, List<BookEntity>>();
        for (var securedBook : securedBooks) {
            var lenderUser = securedBook.lender();
            ownersById.put(lenderUser.getId(), lenderUser);
            bookByOwners.computeIfAbsent(lenderUser.getId(), k -> new ArrayList<>()).add(securedBook.book());
        }
        return bookByOwners.entrySet().stream()
                .map(getTransactionEntityFunction(ownersById))
                .toList();
    }

    private double calculateTotalAmount(List<BookTransactionEntity> transactions) {
        return transactions.stream()
                .map(BookTransactionEntity::getAmount)
                .reduce(0.0, Double::sum);
    }

    private OrderEntity createAndSaveOrder(UserEntity cartOwner, CartEntity cart, List<BookTransactionEntity> transactions, double amount) {
        var order = new OrderEntity();
        order.setStatus(OrderStatus.PENDING);
        order.setCreationDate(Instant.now());
        order.setUser(cartOwner);
        order.setBookTransactions(transactions);
        order.setCartVersion(cart.getVersion());
        order.setAmount(amount);
        return orderRepository.save(order);
    }

    private Function<Map.Entry<UUID, List<BookEntity>>, BookTransactionEntity> getTransactionEntityFunction(HashMap<UUID, UserEntity> ownersById) {
        return entry -> {
            var owner = ownersById.get(entry.getKey());
            var priceEstimation = booqInConfig.priceEstimator().estimatePrice(entry.getValue().size());
            var transaction = new BookTransactionEntity();
            transaction.setBookOwner(owner);
            transaction.setBooks(new HashSet<>(entry.getValue()));
            transaction.setAmount(priceEstimation);
            transaction.setSteps(List.of(TransactionStepEntity.firstStep()));
            return transaction;
        };
    }

    @Transactional
    public void completeOrder(UUID userId, UUID orderId, Long cartVersion, String paymentType, String paymentTxId) {
        lock.lock();
        try {
            var order = orderRepository.findById(orderId, LockModeType.PESSIMISTIC_WRITE)
                    .orElseThrow(() -> new TheirFaultException("Order does not exist"));
            if (!order.getCartVersion().equals(cartVersion)) {
                throw new OurFaultException("Cart version does not match");
            }
            if (!order.getUser().getId().equals(userId)) {
                throw new OurFaultException("Order does not belong to user");
            }
            if (order.getStatus() != OrderStatus.PENDING) {
                throw new OurFaultException("Order is not due");
            }
            order.setStatus(OrderStatus.FULFILLED);
            order.setPaymentType(paymentType);
            order.setPaymentTxId(paymentTxId);
            orderRepository.save(order);
            logger.info("Order `" + orderId + "` completed for user `" + userId + "`");

            // clear cart
            var cart = cartRepository.findByOwnerId(userId, LockModeType.PESSIMISTIC_WRITE)
                    .orElseThrow(() -> new OurFaultException("User does not have a cart"));
            cart.clear();
            cartRepository.save(cart);
        } finally {
            lock.unlock();
        }
    }

}
