package fr.uge.booqin.app.service.loan;


import fr.uge.booqin.app.dto.book.BookWaitListStatusDTO;
import fr.uge.booqin.app.dto.book.LendBookRequest;
import fr.uge.booqin.app.dto.book.UpdateLendBook;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.loan.bookstock.BookStockManager;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.persistence.fixtures.BookFixtures;
import fr.uge.booqin.infra.persistence.fixtures.UserFixtures;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.loan.BookSupplyAndDemandRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        locations = "/application-test.properties",
        properties = {
                "spring.profiles.active=test",
//                 // decrease the lock duration for the test
                "booqin.borrow.waiting-list-lock-duration=PT5S",
                "booqin.borrow.cart-lock-timeout=PT5S",
        }
)
@Transactional
public class LoanServiceIntTest {

    @Autowired
    private BookLoanService bookLoanService;

    @Autowired
    private UserFixtures userFixtures;

    @Autowired
    private BookFixtures bookFixtures;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookSupplyAndDemandRepository bookSupplyAndDemandRepository;

    @Autowired
    private BookService bookService;

    @Autowired
    private CartService cartService;

    @Autowired
    private BookStockManager bookStockManager;

    @Test
    @Rollback
    public void lendABookNobodyWaiting() {
        /// One user lend one book
        /// Nobody is waiting for the book
        var user = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        var book = bookFixtures.randomBook();
        bookLoanService.lendBook(user,
                new LendBookRequest(book.getId(), 1));

        var bookSupply = bookLoanService.getBookSupplyAndDemand(user, book.getId());
        assertEquals(1, bookSupply.supply());
    }

    @Test
    @Rollback
    public void borrowBookNotAvailableGetUserInWaitingList() {
        /// Nobody lend the book
        /// One user set a reservation (goes to the waiting list)
        var book = bookFixtures.randomBook();
        var user = userFixtures.createDummyUsers(1).getFirst();
        bookLoanService.borrowBook(UserMapper.from(user), book.getId());
        var supply = bookSupplyAndDemandRepository.findByBookId(book.getId());
        assertEquals(1, supply.orElseThrow().getDemand());
    }

    @Test
    @Rollback
    public void lendBookOneUserWaiting() {
        /// 1) One user waits for the book (book not available at request time)
        /// 2) One user lend the book
        // given
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId());

        // when
        var userLendingTheBook = users.getLast();
        bookLoanService.lendBook(UserMapper.from(userLendingTheBook),
                new LendBookRequest(book.getId(), 1));

        // then, expect that the waiting user gets a notification that the book is available
        // and locked for him for 1h
        var bookSupplyData = bookLoanService.getBookSupplyAndDemand(userWantingTheBook, book.getId());
        assertEquals(0, bookSupplyData.supply());
        assertEquals(0, bookSupplyData.demand());
        var userBrrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        assertEquals(1, userBrrowings.size());
    }

    @Test
    public void OneUserWaitingForTwoBooksAndOneUserLendOneOfThem() {
        /// 1) One user waits for the book (book not available at request time)
        /// 2) One user lend the book
        // given
        var users = userFixtures.createDummyUsers(2);
        var book1 = bookFixtures.randomBook();
        var book2 = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book1.getId());
        bookLoanService.borrowBook(userWantingTheBook, book2.getId());

        // when
        var userLendingTheBook = UserMapper.from(users.getLast());
        bookLoanService.lendBook(userLendingTheBook,
                new LendBookRequest(book1.getId(), 1));

        // then, the borrowing user should be able to see two borrowings
        // one of them is locked
        // the other one is not
        var userBorrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        assertEquals(2, userBorrowings.size());
        assertTrue(userBorrowings.stream().anyMatch(BookWaitListStatusDTO::isLocked));
        assertTrue(userBorrowings.stream().anyMatch(b -> b.bookId().equals(book2.getId())
                && !b.isLocked()));
    }

    @Test
    public void lendBookOneUserWaitingExpectTheUserLoseTheLockAfterPeriod() throws InterruptedException {
        /// 1) One user waits for the book (book not available at request time)
        /// 2) One user lend the book
        // given
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId());
        var userLendingTheBook = users.getLast();
        bookLoanService.lendBook(UserMapper.from(userLendingTheBook),
                new LendBookRequest(book.getId(), 1));
        // when

        Thread.sleep(6000); // wait for the lock to expire

        // then, expect the user has lost the lock, and got removed from the waiting list
        var userBorrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        assertEquals(0, userBorrowings.size());
    }

    @Test
    public void userInWaitingGetTheLockAndAcceptIt() {
        /// 1) One user waits for the book (book not available at request time)
        /// 2) One user lend the book
        // given
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId());
        var userLendingTheBook = users.getLast();
        bookLoanService.lendBook(UserMapper.from(userLendingTheBook),
                new LendBookRequest(book.getId(), 1));
        // when
        var userBrrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        var proposition = userBrrowings.getFirst();
        bookLoanService.acceptBook(proposition.bookId(), userWantingTheBook);
        // the user isnot in the waiting list anymore
        var userWaitlistStatus = bookLoanService.getWaitlistStatus(userWantingTheBook);
        assertEquals(0, userWaitlistStatus.size());
        // the book supply is still 0
        var bookSupplyData = bookLoanService.getBookSupplyAndDemand(userWantingTheBook, book.getId());
        assertEquals(0, bookSupplyData.supply());
    }

    @Test
    public void userAcceptBookAndLoseLockInCart() throws InterruptedException {
        /// 1) Another user waits for the book (book not available at request time)
        /// 2) Another user lend the book
        // given
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();

        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId());

        var userLendingTheBook = UserMapper.from(users.getLast());
        bookLoanService.lendBook(userLendingTheBook, new LendBookRequest(book.getId(), 1));

        // when, the user accepts the book
        var userBrrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        var proposition = userBrrowings.getFirst();
        bookLoanService.acceptBook(proposition.bookId(), userWantingTheBook);

        Thread.sleep(7000); // wait for the lock to expire

        // then the supply should be available again
        // (lock from cart expired, get released, and the supply goes back to 1)
        var bookSupplyData = bookLoanService.getBookSupplyAndDemand(userWantingTheBook, book.getId());
        assertEquals(1, bookSupplyData.supply());
    }

    @Test
    public void userAcceptBookAndCheckTheCartHavingALock() {
        /// 1) One user waits for the book (book not available at request time)
        /// 2) One user lend the book
        // given
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId());
        var userLendingTheBook = users.getLast();
        bookLoanService.lendBook(UserMapper.from(userLendingTheBook),
                new LendBookRequest(book.getId(), 1));
        // when
        var userBrrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        var proposition = userBrrowings.getFirst();
        bookLoanService.acceptBook(proposition.bookId(), userWantingTheBook);
        // the user isnot in the waiting list anymore
        var userWaitlistStatus = bookLoanService.getWaitlistStatus(userWantingTheBook);
        assertEquals(0, userWaitlistStatus.size());
        // the book supply is still 0
        var bookSupplyData = bookLoanService.getBookSupplyAndDemand(userWantingTheBook, book.getId());
        assertEquals(0, bookSupplyData.supply());
        // the user should have the book in his cart
        var cart = cartService.getCart(userWantingTheBook);
        assertEquals(1, cart.books().size());
        assertEquals(book.getId(), cart.books().getFirst().book().id());
        assertTrue(cart.books().getFirst().locked());
    }

    @Test
    public void userCantBorrowBookThenLendTheSameBook() {
        // given
        var users = UserMapper.from(userFixtures.createDummyUsers(1).getFirst());
        var book = bookFixtures.randomBook();
        bookLoanService.borrowBook(users, book.getId());
        // then
        assertThrows(TheirFaultException.class, () -> {
            // when
            bookLoanService.lendBook(users, new LendBookRequest(book.getId(), 1));
        });
/*        */
    }

    @Test
    public void userInWaitlistHasProperInfoOnSupply() {
        // given, one user wiching for a book
        // and one user lending the book
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId()); // book not available yeat
        var userLendingTheBook = users.getLast();
        bookLoanService.lendBook(UserMapper.from(userLendingTheBook),
                new LendBookRequest(book.getId(), 1)); // book is available now
        // the first use get a proposition
        var supplyInfo = bookLoanService.getBookSupplyAndDemand(userWantingTheBook, book.getId());
        System.out.println(supplyInfo);
        assertFalse(supplyInfo.isLent());
        assertTrue(supplyInfo.isBorrowed());
    }

    @Test
    public void borrowUnborrowBook() {
        // given
        var users = userFixtures.createDummyUsers(1);
        var book = bookFixtures.randomBook();
        var user = UserMapper.from(users.getFirst());
        // when
        bookLoanService.borrowBook(user, book.getId());
        var supplyBefore = bookSupplyAndDemandRepository.findByBookId(book.getId());
        assertEquals(1, supplyBefore.orElseThrow().getDemand());
        bookLoanService.unBorrowBook(user, book.getId());
        // then
        var supplyafter = bookSupplyAndDemandRepository.findByBookId(book.getId());
        assertEquals(0, supplyafter.orElseThrow().getDemand());
    }

    @Test
    public void userAcceptPropositionThatIsNoMoreValid() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var book = bookFixtures.randomBook();
        var userWantingTheBook = UserMapper.from(users.getFirst());
        bookLoanService.borrowBook(userWantingTheBook, book.getId());
        var userLendingTheBook = UserMapper.from(users.getLast());
        bookLoanService.lendBook(userLendingTheBook, new LendBookRequest(book.getId(), 1));
        // userWantingTheBook has a proposition
        // when
        var userBrrowings = bookLoanService.getWaitlistStatus(userWantingTheBook);
        var proposition = userBrrowings.getFirst();
        assertTrue(proposition.isLocked());
        // then
        bookStockManager.updateOrRemove(userLendingTheBook, new UpdateLendBook(book.getId(), 0));
        // so the proposition is no more valid (the book is not available anymore)
        // user will receive a notification that it expired
        assertThrows(TheirFaultException.class, () -> bookLoanService.acceptBook(proposition.bookId(), userWantingTheBook));
        assertFalse(cartService.isBookInCart(userWantingTheBook, book.getId()));
    }



}
