package fr.uge.booqin.app.service;

import fr.uge.booqin.app.dto.book.LendBookRequest;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.loan.BookLoanService;
import fr.uge.booqin.app.service.loan.CartService;
import fr.uge.booqin.domain.model.cart.OrderStatus;
import fr.uge.booqin.domain.model.cart.TransactionStepType;
import fr.uge.booqin.infra.persistence.fixtures.BookFixtures;
import fr.uge.booqin.infra.persistence.fixtures.UserFixtures;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(
        locations = "/application-test.properties",
        properties = {
                "spring.profiles.active=test"
        }
)
@Transactional
public class CartServiceIntTest {
    @Autowired
    private BookLoanService bookLoanService;

    @Autowired
    private UserFixtures userFixtures;

    @Autowired
    private BookFixtures bookFixtures;

    @Autowired
    private CartService cartService;

    @Test
    @Rollback
    void checkoutCart() {
        // given
        // a user lending a book
        var lender = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        var book = bookFixtures.randomBook();
        bookLoanService.lendBook(lender, new LendBookRequest(book.getId(), 1));
        // a user borrowing a book
        var borrower = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        bookLoanService.borrowBook(borrower, book.getId());
        // the borrower has a cart with the book
        var cart = cartService.getCart(borrower);
        // when
        var checkout = cartService.checkout(borrower, cart.version());
        // then
        assertEquals(1, checkout.order().bookTransactions().size());
        assertEquals(OrderStatus.PENDING, checkout.order().status());
        // the unique transaction it exists in the cart
        var transaction = checkout.order().bookTransactions().getFirst();
        assertEquals(TransactionStepType.TO_BE_SENT, transaction.steps().getFirst().type());
    }

    @Test
    @Rollback
    void checkoutCartWithMultipleBooksFromOneOwner() {
        // given
        // a user lending a book
        var lender = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        var book1 = bookFixtures.randomBook();
        var book2 = bookFixtures.randomBook();
        bookLoanService.lendBook(lender, new LendBookRequest(book1.getId(), 1));
        bookLoanService.lendBook(lender, new LendBookRequest(book2.getId(), 1));
        // a user borrowing a book
        var borrower = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        bookLoanService.borrowBook(borrower, book1.getId());
        bookLoanService.borrowBook(borrower, book2.getId());
        // the borrower has a cart with the book
        var cart = cartService.getCart(borrower);
        // when
        var checkout = cartService.checkout(borrower, cart.version());
        // then
        assertEquals(1, checkout.order().bookTransactions().size()); // only one transaction because the
                                                                              // lender is the same for both books
        assertEquals(OrderStatus.PENDING, checkout.order().status());
        var transaction = checkout.order().bookTransactions().getFirst();
        assertEquals(TransactionStepType.TO_BE_SENT, transaction.steps().getFirst().type());
        assertEquals(checkout.order().amount(), transaction.amount());
    }

    @Test
    @Rollback
    void checkoutCartWithMultipleBooksFromTwoOwners() {
        // given
        // users lending a book
        var lender1 = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        var book1 = bookFixtures.randomBook();
        bookLoanService.lendBook(lender1, new LendBookRequest(book1.getId(), 1));

        var lender2 = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        var book2 = bookFixtures.randomBook();
        bookLoanService.lendBook(lender2, new LendBookRequest(book2.getId(), 1));
        // a user borrowing books
        var borrower = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        bookLoanService.borrowBook(borrower, book1.getId());
        bookLoanService.borrowBook(borrower, book2.getId());
        // the borrower has a cart with the book
        var cart = cartService.getCart(borrower);
        // when
        var checkout = cartService.checkout(borrower, cart.version());
        // then
        assertEquals(2, checkout.order().bookTransactions().size()); // two lenders => two transactions
        assertEquals(OrderStatus.PENDING, checkout.order().status());
        var transaction1 = checkout.order().bookTransactions().getFirst();
        assertEquals(TransactionStepType.TO_BE_SENT, transaction1.steps().getFirst().type());
        var transaction2 = checkout.order().bookTransactions().getFirst();
        assertEquals(TransactionStepType.TO_BE_SENT, transaction2.steps().getFirst().type());
        assertEquals(checkout.order().amount(), transaction1.amount() + transaction2.amount());
    }




}
