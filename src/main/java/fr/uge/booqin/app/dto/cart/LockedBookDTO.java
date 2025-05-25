package fr.uge.booqin.app.dto.cart;

import fr.uge.booqin.app.dto.book.BookInfoDTO;

import java.time.Instant;

public record LockedBookDTO(BookInfoDTO book, boolean locked, Instant lockedUntil) {
    }