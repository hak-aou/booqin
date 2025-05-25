package fr.uge.booqin.app.dto.book;

import fr.uge.booqin.domain.model.books.ImageFormatModel;
import fr.uge.booqin.domain.model.books.IsbnModel;

import java.util.List;
import java.util.UUID;

public record BookInfoDTO(
        UUID id,
        UUID commentableId,
        UUID followableId,
        IsbnModel isbn,
        String title,
        String subtitle,
        List<String> categories,
        ImageFormatModel imageLinks,
        Integer supply
) {
}
