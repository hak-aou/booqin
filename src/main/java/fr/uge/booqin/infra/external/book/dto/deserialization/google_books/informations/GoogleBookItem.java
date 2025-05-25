package fr.uge.booqin.infra.external.book.dto.deserialization.google_books.informations;

public record GoogleBookItem(
        String kind,
        String id,
        String etag,
        String selfLink,
        GoogleVolumeInfo volumeInfo
        // SalesInfo saleInfo,
        // AccessInfo accessInfo,
        // SearchInfo searchInfo,
) {
}
