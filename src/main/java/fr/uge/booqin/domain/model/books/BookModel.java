package fr.uge.booqin.domain.model.books;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BookModel(
        UUID votableId,
        String title,
        UUID id,
        UUID commentableId,
        UUID followableId,
        IsbnModel isbn,
        List<String> authors,

        List<String> publishers,
        LocalDate publishedDate,

        List<String> categories,
        String language,
        ImageFormatModel imageLinks,

        String subtitle,
        String description,
        Integer pageCount
) {
}