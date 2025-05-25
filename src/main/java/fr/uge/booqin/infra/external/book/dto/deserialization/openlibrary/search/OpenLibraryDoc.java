package fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.search;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record OpenLibraryDoc(
        // String author_key,
        @JsonProperty("author_name") List<String> authors,
        //Integer cover_i,
        //Integer edition_count,
        //Integer first_publish_year,
        //boolean has_fulltext,
        // List<String> ia,
        // String ia_collection_s,
        @JsonProperty("key") String workId // "/works/.../editions.json"
        // @JsonProperty("language") List<String> languages
        // boolean public_scan_b,
        // String title
) {
}