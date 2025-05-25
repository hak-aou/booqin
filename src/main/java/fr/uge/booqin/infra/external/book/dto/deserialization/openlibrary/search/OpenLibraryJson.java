package fr.uge.booqin.infra.external.book.dto.deserialization.openlibrary.search;

import java.util.List;

// // https://openlibrary.org/search.json?q={query}&language:{language}.json
public record OpenLibraryJson (
    Integer numFound,
    Integer start,
    boolean numFoundExact,
    Integer num_found,
    String documentation_url,
    String q,
    String offset,
    List<OpenLibraryDoc> docs
) {}