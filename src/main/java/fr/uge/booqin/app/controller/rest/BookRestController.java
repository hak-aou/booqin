package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.book.LendBookRequest;
import fr.uge.booqin.app.dto.book.UpdateLendBook;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.loan.BookExchangeInfo;
import fr.uge.booqin.app.service.loan.BookLoanService;
import fr.uge.booqin.app.service.loan.bookstock.BookStockManager;
import fr.uge.booqin.app.dto.book.BookWaitListStatusDTO;
import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import java.util.List;


@RestController
@RequestMapping({"/api/books", "/android/books"})
public class BookRestController {

    private final BookService bookService;
    private final BookLoanService bookLoanService;
    private final BookStockManager bookStockManager;


    public BookRestController(BookService bookService,
                                BookLoanService bookLoanService,
                                BookStockManager bookStockManager
                              ) {
        this.bookService = bookService;
        this.bookLoanService = bookLoanService;
        this.bookStockManager = bookStockManager;
    }

    // http://localhost:5173/book/9780698158542
    // http://localhost:5173/book/9780399546846
    @GetMapping("/{bookId}")
    public BookModel getBookData(
            @PathVariable("bookId") UUID bookId
    ) {
        //return bookService.findBookByIsbn13(isbn13);
        return bookService.findBookByIdToModel(bookId);
    }

    @GetMapping("/isbn/{isbn13}")
    public BookModel getBookData(
            @PathVariable("isbn13") String isbn13
    ) {
        return bookService.findBookByIsbn13(isbn13);
    }


    @PostMapping("/{bookId}/borrow")
    public void borrowBook(
            @PathVariable("bookId") UUID bookId,
            @AuthenticationPrincipal SecurityUser currentUser
    ) {
        var authenticatedUser = currentUser.authenticatedUser();
        bookLoanService.borrowBook(authenticatedUser, bookId);
    }

    @DeleteMapping("/{bookId}/borrow")
    public void unborrow(
            @PathVariable("bookId") UUID bookId,
            @AuthenticationPrincipal SecurityUser currentUser
    ) {
        bookLoanService.unBorrowBook(currentUser.authenticatedUser(), bookId);
    }

    @PostMapping("/lend")
    public void lend(
             @RequestBody @Valid LendBookRequest lendBookRequest,
             @AuthenticationPrincipal SecurityUser currentUser
    ) {
        bookLoanService.lendBook(currentUser.authenticatedUser(), lendBookRequest);
    }

    @PatchMapping("/lend/{bookId}")
    public void updateLend(
            @PathVariable("bookId") UUID bookId,
            @AuthenticationPrincipal SecurityUser currentUser
    ) {
        var updateLendBook = new UpdateLendBook(bookId, 0);
        bookStockManager.updateOrRemove(currentUser.authenticatedUser(), updateLendBook);
    }

    @GetMapping("/{bookId}/availability")
    public BookExchangeInfo getBookAvailability(
            @PathVariable("bookId") UUID bookId,
            @AuthenticationPrincipal SecurityUser currentUser
    ) {
        return bookLoanService.getBookSupplyAndDemand(currentUser.authenticatedUser(), bookId);
    }

    @GetMapping("/languages")
    public List<String> getAllLanguages() {
        return bookService.getAllLanguages();
    }

    @GetMapping("/categories")
    public List<String> getAllCategories() {
        return bookService.getAllCategories();
    }

    @PostMapping("/{bookId}/waitlistAccept")
    public void WaitlistAccept(
            @PathVariable("bookId") UUID bookId,
            @AuthenticationPrincipal SecurityUser currentUser
    ) {
        bookLoanService.acceptBook(bookId , currentUser.authenticatedUser());
    }


    @GetMapping("/waitlists")
    public List<BookWaitListStatusDTO> getWaitLists(@AuthenticationPrincipal SecurityUser currentUser) {
        return bookLoanService.getWaitlistStatus(currentUser.authenticatedUser());
    }

    @GetMapping("/loans")
    public List<BookStockManager.LoanOfferDTO> getLoans(@AuthenticationPrincipal SecurityUser currentUser) {
        return bookStockManager.myOffers(currentUser.authenticatedUser());
    }

}

