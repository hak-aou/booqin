package fr.uge.booqin.infra.external.book.adapter;


import fr.uge.booqin.app.dto.book.BookMapper;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.GoogleBooksJson;
import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.informations.GoogleBookItem;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.isbns.GoogleIsbnStructure;
import fr.uge.booqin.infra.external.book.query_parameter.QueryLanguage;
import fr.uge.booqin.infra.external.book.query_parameter.QueryParameterBuilder;
import fr.uge.booqin.infra.utils.aspect.Timed;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

// https://developers.google.com/books

// https://www.googleapis.com/books/v1/volumes?q=isbn=9780201633610
public class GoogleBooksAPIAdapter implements BookAPI {

    private static final int LIMIT_SIZE = 40; // 40 is the max for the Google books api

    private final WebClient webClient;

    public GoogleBooksAPIAdapter(WebClient webClient) {
        this.webClient = webClient;
    }

    @Timed
    @Override
    public List<BookModel> getBookMetadata(QueryParameterBuilder queryParameterAPI) {
        var offset = 0;
        List<GoogleBookItem> items = new ArrayList<>();

        try {

            while (true) {
                var url = queryParameterAPI.limit(LIMIT_SIZE).offset(offset).build();
                var metadata = getMetadata(url);

                // No books found in the Google books api
                if (metadata == null || metadata.items() == null) {
                    return List.of();
                }

                // Books can have 'OTHER' as ISBN so we must call the selfLink to get the correct ISBN.
                // Get the correct ISBN from the selfLink that was previously found as OTHER
                var selfLinks = getSelflinkWhenNoIsbn(metadata.items());
                var newBooks = getBooksData(selfLinks);

                items.addAll(newBooks);
                items.addAll(metadata.items());

                if (metadata.items().size() < LIMIT_SIZE) {
                    break;
                }

                offset += LIMIT_SIZE;
            }

            return filterUsefulBooks(items);
        } catch (Exception e) {
            System.err.println("Error when fetching data from Google Books : " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Timed
    private GoogleBooksJson getMetadata(String url) {
        try {
            return webClient.get()
                    .uri(url)
                    .retrieve()
                    .bodyToMono(GoogleBooksJson.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Google book api error when fetching " + url + " : " + e.getMessage());
        }
    }

    @Timed
    private List<String> getSelflinkWhenNoIsbn(List<GoogleBookItem> items) {
        return items.stream()
                .map(googleBookItem -> {
                    var googleVolumeInfo = googleBookItem.volumeInfo();
                    if (googleVolumeInfo.isbns() == null) {
                        return null;
                    }
                    var types = googleVolumeInfo.isbns().stream().map(GoogleIsbnStructure::type).toList();
                    if (types.contains("OTHER") && !types.contains("ISBN_13")) {
                        return googleBookItem.selfLink();
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .toList();
    }

    @Timed
    private List<GoogleBookItem> getBooksData(List<String> selfLinks) {
        return selfLinks.stream()
                .map(link -> webClient.get()
                        .uri(link)
                        .retrieve()
                        .bodyToMono(GoogleBookItem.class)
                        .block())
                .filter(Objects::nonNull)
                .toList();
    }

    @Timed
    private List<BookModel> filterUsefulBooks(List<GoogleBookItem> items) {
        return items.stream()
                .map(GoogleBookItem::volumeInfo)
                .filter(gvi -> gvi.isbns() != null && gvi.language() != null)
                .filter(gvi -> gvi.isbns().stream().anyMatch(isbn -> "ISBN_13".equals(isbn.type())))
                .filter(gbi -> QueryLanguage.getLanguageByAbbreviation(gbi.language()) != null)
                .map(BookMapper::fromDTOtoModel)
                .toList();
    }

}
