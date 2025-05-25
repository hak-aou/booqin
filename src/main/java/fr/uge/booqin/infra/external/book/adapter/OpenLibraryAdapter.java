package fr.uge.booqin.infra.external.book.adapter;


import fr.uge.booqin.app.dto.book.BookMapper;
import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.book.OpenLibraryBook;
import fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.search.OpenLibraryJson;
import fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.search.OpenLibraryDoc;
import fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.works.OpenLibraryEditions;
import fr.uge.booqin.infra.external.book.query_parameter.QueryParameterBuilder;
import fr.uge.booqin.infra.utils.aspect.Timed;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

// https://openlibrary.org/developers/api
// https://openlibrary.org/api/books?bibkeys=ISBN:9780201633610&format=json&jscmd=data

// https://openlibrary.org/search.json?q=Amber%20Dermont&language:eng&format=json&jscmd=data
// https://openlibrary.org/works/OL16215444W/editions.json
public class OpenLibraryAdapter implements BookAPI {

    private static final String OPENLIBRARY_URL = "https://openlibrary.org/";
    private static final String OPENLIBRARY_SEARCH_URL = "https://openlibrary.org/search.json";
    private static final int LIMIT_SIZE = 50;
    private static final Set<String> ALLOWED_FORMATS = Set.of("Paperback",
            "Hardcover",
            "Spiral-bound",
            "mass market paperback",
            "board book"
    ); // source : https://github.com/internetarchive/openlibrary-client/blob/master/olclient/schemata/edition.schema.json

    private final WebClient webClient;

    public OpenLibraryAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Timed
    @Override
    public List<BookModel> getBookMetadata(QueryParameterBuilder queryParameterAPI) {
        try {
            List<OpenLibraryBook> openLibraryBooks = new ArrayList<>();

            // Get all books from author
            List<OpenLibraryDoc> works = getWorksIdFromSearch(queryParameterAPI);

            // Get all edition from one book
            for (var work : works) {
                var books = getBooksFromWork(work.workId());

                // For each book get its data
                for (var bookId : books) {
                    OpenLibraryBook book = getDataFromBookId(bookId);

                    if (book.isbn_13() != null) {
                        book.authors().addAll(work.authors());
                        openLibraryBooks.add(book);
                    }
                }
            }

            return openLibraryBooks.stream()
                    .filter(Objects::nonNull)
                    .map(BookMapper::fromDTOtoModel)
                    .toList();
        } catch (Exception e) {
            System.err.println("Error when fetching data from OpenLibrary : " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Timed
    private List<OpenLibraryDoc> getWorksIdFromSearch(QueryParameterBuilder queryParameterAPI) {

        var url = queryParameterAPI.build() + ".json";

        try {
            OpenLibraryJson openLibraryJson = webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(OpenLibraryJson.class)
                    .block();

            return openLibraryJson.docs();
        } catch (Exception e) {
            throw new RuntimeException("Openlibrary error when fetching " + url + " : " + e.getMessage());
        }
    }

    @Timed
    private List<String> getBooksFromWork(String workId) {
        var offset = 0;
        List<String> bookIds = new ArrayList<>();
        OpenLibraryEditions openLibraryEditions;

        do {
            var query_param = String.format("?limit=%d&offset=%d", LIMIT_SIZE, offset);
            var url = OPENLIBRARY_URL + workId + "/editions.json" + query_param;

            try {

                openLibraryEditions = webClient.get()
                        .uri(url)
                        .retrieve()
                        .bodyToMono(OpenLibraryEditions.class)
                        .block();

                var books = openLibraryEditions
                        .entries()
                        .stream()
                        .filter(openLibraryEntries -> openLibraryEntries.languages() != null && !openLibraryEntries.languages().isEmpty())
                        .filter(openLibraryEntries -> openLibraryEntries.physical_format() == null || hasAllowedFormat(openLibraryEntries.physical_format()))
                        .map(OpenLibraryEditions.OpenLibraryEntries::bookData)
                        .toList();

                bookIds.addAll(books);

                offset += LIMIT_SIZE;

            } catch (Exception e) {
                throw new RuntimeException("Openlibrary error when fetching " + url + " : " + e.getMessage());
            }
        } while (openLibraryEditions.entries().size() == LIMIT_SIZE);

        return bookIds;
    }

    @Timed
    private OpenLibraryBook getDataFromBookId(String bookId) {
        var url = OPENLIBRARY_URL + bookId + ".json";

        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(OpenLibraryBook.class)
                    .block();

        } catch (Exception e) {
            throw new RuntimeException("Openlibrary error when fetching " + url + " : " + e.getMessage());
        }
    }

    @Timed
    public static boolean hasAllowedFormat(String element) {
        if (element == null) {
            return false;
        }
        return ALLOWED_FORMATS.stream().anyMatch(format -> element.toLowerCase().contains(format.toLowerCase()));
    }
}
