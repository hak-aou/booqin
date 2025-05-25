package fr.uge.booqin.app.service;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.book.UpdateLendBook;
import fr.uge.booqin.app.dto.cart.*;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.loan.BookExchangeInfo;
import fr.uge.booqin.app.service.loan.bookstock.BookStockManager;
import fr.uge.booqin.app.service.observer.obs_interface.TransactionObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.cart.OrderStatus;
import fr.uge.booqin.domain.model.cart.TransactionStepType;
import fr.uge.booqin.domain.model.cart.TransactionWorkflowResolver;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.persistence.entity.cart.BookTransactionEntity;
import fr.uge.booqin.infra.persistence.entity.cart.OrderEntity;
import fr.uge.booqin.infra.persistence.entity.cart.TransactionStepEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.cart.BookTransactionRepository;
import fr.uge.booqin.infra.persistence.repository.cart.OrderRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class TransactionService {

    private final OrderRepository orderRepository;
    private final BookTransactionRepository bookTransactionRepository;
    private final BookService bookService;
    private final BookStockManager bookStockManager;
    private final TransactionWorkflowResolver transactionWorkFlowResolver;
    private final List<TransactionObserver> observers = new ArrayList<>();

    public TransactionService(OrderRepository orderRepository,
                              BookTransactionRepository bookTransactionRepository,
                              BookService bookService,
                              BookStockManager bookStockManager,
                              TransactionWorkflowResolver transactionWorkFlowResolver,
                              List<TransactionObserver> observers) {
        this.orderRepository = orderRepository;
        this.bookTransactionRepository = bookTransactionRepository;
        this.bookService = bookService;
        this.bookStockManager = bookStockManager;
        this.transactionWorkFlowResolver = transactionWorkFlowResolver;
        this.observers.addAll(observers);
    }

    @Transactional
    public List<Order> myBorrowsTransactions(User user) {
        return orderRepository.findByUserIdAndStatus(user.id(), OrderStatus.FULFILLED).stream()
                .map(this::from)
                .toList();
    }

    @Transactional
    public List<BookLendTransaction> myLendTransactions(User user) {
        return bookTransactionRepository.findByBookOwner(user.id(), OrderStatus.FULFILLED)
                .stream()
                .map(bookTransaction -> {
                    var steps = bookTransaction.getSteps().stream()
                            .map(step -> new TransactionStep(step.getId(), step.getType(), step.getTimestamp()))
                            .toList();
                    var books = bookTransaction.getBooks().stream()
                            .map(bookService::from)
                            .toList();
                    return new BookLendTransaction(
                            bookTransaction.getId(),
                            books,
                            steps,
                            UserService.from(bookTransaction.getOrder().getUser()));
                })
                .toList();
    }

    ///
    /// This method is responsible for moving the transaction to the next step in the workflow
    ///
    @Transactional
    public void nextStep(User user, UUID txId) {
        var transaction = bookTransactionRepository.findByTxIdByBorrowerOrByLender(txId, user.id())
                .orElseThrow(() -> new TheirFaultException("Transaction does not exist"));
        var latestStep = transaction.getSteps().stream()
                .max(Comparator.comparing(TransactionStepEntity::getTimestamp))
                .orElseThrow(() -> new OurFaultException("Transaction has no steps"));
        TransactionStepType nextStep;

        UserEntity origin;
        UserEntity target;
        if(transaction.getBookOwner().getId().equals(user.id())) {
            nextStep = transactionWorkFlowResolver.nextAsLender(latestStep.getType())
                    .orElseThrow(() -> new OurFaultException("No next step"));
            origin = transaction.getBookOwner();
            target = transaction.getOrder().getUser();
        } else if (transaction.getOrder().getUser().getId().equals(user.id())) {
            nextStep = transactionWorkFlowResolver.nextAsBorrower(latestStep.getType())
                    .orElseThrow(() -> new OurFaultException("No next step"));
            origin = transaction.getOrder().getUser();
            target = transaction.getBookOwner();
        } else {
            throw new TheirFaultException("The order or the transaction doesn't exists");
        }
        var newStep = TransactionStepEntity.of(nextStep);
        transaction.addStep(newStep);
        bookTransactionRepository.save(transaction);
        // notify observers
        observers.forEach(o -> o.transactionUpdated(
                UserMapper.from(origin),
                UserMapper.from(target),
                // the lender doesn't have to know the orderId
                Optional.ofNullable(target.getId().equals(transaction.getBookOwner().getId()) ? null : transaction.getOrder().getId()),
                transaction.getId(),
                nextStep));
        // give back the book to the owner by adding it to the stock again
        if(nextStep == TransactionStepType.RECEIVED_BACK) {
            for (var book : transaction.getBooks()) {
                bookStockManager.incrDecrOffer(UserMapper.from(transaction.getBookOwner()), new UpdateLendBook(book.getId(), 1));
            }
        }
    }

    public Order from(OrderEntity order) {
        var bookTransactions = order.getBookTransactions().stream()
                .map(bookTransaction -> {
                    var steps = bookTransaction.getSteps().stream()
                            .map(step -> new TransactionStep(step.getId(), step.getType(), step.getTimestamp()))
                            .toList();
                    var books = bookTransaction.getBooks().stream()
                            .map(bookService::from)
                            .toList();
                    return getBookTransaction(bookTransaction, books, steps);
                })
                .toList();
        return new Order(
                order.getId(),
                order.getUser().getId(),
                order.getCartVersion(),
                order.getCreationDate(),
                order.getStatus(),
                order.getAmount(),
                order.getPaymentType(),
                order.getPaymentTxId(),
                bookTransactions
        );
    }

    private static BookTransaction getBookTransaction(BookTransactionEntity bookTransaction,
                                                      List<BookInfoDTO> books,
                                                      List<TransactionStep> steps) {
        var owner = new OwnerProfile(
                bookTransaction.getBookOwner().getId(),
                bookTransaction.getBookOwner().getUsername(),
                bookTransaction.getBookOwner().getImageUrl()
        );
        return new BookTransaction(
                bookTransaction.getId(),
                bookTransaction.getBookOwner().getId(),
                owner,
                books,
                bookTransaction.getAmount(),
                steps);
    }

    @Transactional
    public Optional<BookExchangeInfo.InTx> findTxByBook(UUID userId, UUID bookId) {
        var aa = bookTransactionRepository.findFirstByBookIdAndBorrowerOrLender(bookId, userId, PageRequest.of(0, 1),
                        TransactionStepType.RECEIVED_BACK);

       return aa         .stream()
                .findFirst()
                .map(tx -> new BookExchangeInfo.InTx(
                        tx.getBookOwner().getId().equals(userId) ? null :  tx.getOrder().getId(),
                        tx.getId()));
    }
}
