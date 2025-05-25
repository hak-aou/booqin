package fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.works;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenLibraryEditions(
        // String links,
        Integer size,
        List<OpenLibraryEntries> entries
) {

    public record OpenLibraryEntries(
            @JsonProperty("key") String bookData, // "/books/..."
            @JsonProperty("languages") List<Language> languages,
            @JsonProperty("physical_format") String physical_format
    ) {}

    public record Language(
            @JsonProperty("key") String key
    ) {}
}