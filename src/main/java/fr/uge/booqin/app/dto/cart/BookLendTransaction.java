package fr.uge.booqin.app.dto.cart;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;

import java.util.List;
import java.util.UUID;

public record BookLendTransaction(
            UUID txId,
            List<BookInfoDTO> books,
            List<TransactionStep> steps,
            PublicProfileDTO user
    ){}
