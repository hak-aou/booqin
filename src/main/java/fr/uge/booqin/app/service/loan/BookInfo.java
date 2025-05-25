package fr.uge.booqin.app.service.loan;

import fr.uge.booqin.domain.model.books.ImageFormatModel;

import java.util.UUID;

public record BookInfo(
        UUID bookId,
        String title,
        ImageFormatModel images
){}