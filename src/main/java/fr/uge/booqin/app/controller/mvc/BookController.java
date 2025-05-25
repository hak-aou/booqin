package fr.uge.booqin.app.controller.mvc;

import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.domain.model.books.IsbnModel;
import fr.uge.booqin.infra.external.book.adapter.BookAPI;
import fr.uge.booqin.infra.external.book.adapter.GoogleBooksAPIAdapter;
import fr.uge.booqin.infra.external.book.adapter.OpenLibraryAdapter;
import fr.uge.booqin.infra.external.book.query_parameter.GoogleBookQueryParameterBuilder;
import fr.uge.booqin.infra.external.book.query_parameter.OpenLibraryQueryParameterBuilder;
import fr.uge.booqin.infra.external.book.query_parameter.QueryLanguage;
import fr.uge.booqin.infra.external.book.query_parameter.QueryParameterBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Controller
public class BookController {

    private final WebClient webClient;

    private final BookService bookService;

    public BookController(BookService bookService, WebClient webClient) {
        this.bookService = bookService;
        this.webClient = webClient;
    }

    // localhost:8080/books/9780399169922
    @GetMapping("/books/{bookId}")
    public String getBookMetadata(
            @PathVariable("bookId") String isbn13,
            Model model
    ) {
        BookModel book = bookService.findBookByIsbn13(isbn13);
        model.addAttribute("book", book);
        return "book_metadata";
    }

    // localhost:8080/get/books/isbns
    @GetMapping("/get/books/isbns")
    public String getAllBooksIsbns(
            Model model
    ) {
        List<IsbnModel> isbns = bookService.getAllIsbns();
        model.addAttribute("isbns", isbns);
        return "isbns";
    }

    // localhost:8080/googlebooks/Fredrick Barton
    @GetMapping("/googlebooks/{author}")
    public String googleBooksMetadata(
            @PathVariable("author") String author,
            Model model
    ) {
        BookAPI bookAPI = new GoogleBooksAPIAdapter(webClient);

        QueryParameterBuilder queryParameter = new GoogleBookQueryParameterBuilder().author(author).language(QueryLanguage.ENGLISH);
        List<BookModel> books = bookAPI.getBookMetadata(queryParameter);

        model.addAttribute("books", books);
        return "authors_books";
    }

    // localhost:8080/openlibrary/Fredrick Barton
    @GetMapping("/openlibrary/{author}")
    public String openLibraryMetadata(
            @PathVariable("author") String author,
            Model model
    ) {
        BookAPI bookAPI = new OpenLibraryAdapter(webClient);

        QueryParameterBuilder queryParameter = new OpenLibraryQueryParameterBuilder().author(author).language(QueryLanguage.ENGLISH);
        List<BookModel> books = bookAPI.getBookMetadata(queryParameter);

        model.addAttribute("books", books);
        return "authors_books";
    }




}

