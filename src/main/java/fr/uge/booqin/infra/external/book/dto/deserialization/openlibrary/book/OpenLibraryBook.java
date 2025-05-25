package fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.book;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.ArrayList;
import java.util.List;

public record OpenLibraryBook(
        String title,

        List<String> isbn_13,
        List<String> isbn_10,

        @JsonIgnore List<String> authors,

        List<String> publishers,
        String publish_date,

        List<String> subjects,
        List<Language> languages,
        List<Integer> covers,

        String subtitle,

        @JsonDeserialize(using = DescriptionDeserializer.class)
        String description,
        Integer number_of_pages
) {
    public OpenLibraryBook {
        authors = authors == null ? new ArrayList<>() : authors;
    }

    public record Description(
            String type,
            String value
    ) {}

    public record Language(
            String key
    ) {}

    public record Isbn(
            String isbn_13,
            String isbn_10
    ) {}

}