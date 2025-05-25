package fr.uge.booqin.infra.external.book.dto.deserialization.google_books;

import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.informations.GoogleBookItem;

import java.util.List;

public record GoogleBooksJson(
        String kind,
        long totalItems,
        List<GoogleBookItem> items
) {
}
