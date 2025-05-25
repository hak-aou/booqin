package fr.uge.booqin.infra.external.book.dto.deserialization.google_books.informations;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.images.GoogleImageLinks;
import fr.uge.booqin.infra.external.book.dto.deserialization.google_books.isbns.GoogleIsbnStructure;

import java.util.List;

public record GoogleVolumeInfo (
        String title,
        String subtitle,
        List<String> authors,
        String publisher,
        String publishedDate,
        String description,

        @JsonProperty("industryIdentifiers")
        List<GoogleIsbnStructure> isbns,

        // String readingModes;
        Integer pageCount,
        // private String printType;
        List<String> categories,
        // String maturityRating;
        // String allowAnonLogging;
        // String contentVersion;
        // String panelizationSummary;
        GoogleImageLinks imageLinks,
        String language
        // String previewLink;
        // String infoLink;
        // String canonicalVolumeLink;
) {

}







