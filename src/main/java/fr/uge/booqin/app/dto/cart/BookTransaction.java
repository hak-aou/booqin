package fr.uge.booqin.app.dto.cart;

import fr.uge.booqin.app.dto.book.BookInfoDTO;

import java.util.List;
import java.util.UUID;

public record BookTransaction(
            UUID txId,
            UUID ownerId,
            OwnerProfile ownerProfile,
            List<BookInfoDTO> books,
            Double amount,
            List<TransactionStep> steps) {
    }
