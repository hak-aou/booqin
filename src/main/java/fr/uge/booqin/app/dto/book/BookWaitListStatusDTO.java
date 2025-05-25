package fr.uge.booqin.app.dto.book;

import fr.uge.booqin.app.service.loan.BookLoanService;
import fr.uge.booqin.domain.model.books.ImageFormatModel;

import java.time.Instant;
import java.util.UUID;

public record BookWaitListStatusDTO(
        UUID bookId,
        String title,
        ImageFormatModel images,
        boolean isLocked,
        Instant lockedUntil
) {

    public static BookWaitListStatusDTO from(BookLoanService.PropositionLockRecord proposition) {
        return new BookWaitListStatusDTO(
                proposition.lock().bookId(),
                proposition.lock().bookInfo().title(),
                proposition.lock().bookInfo().images(),
                true,
                proposition.lock().expirationTime()
        );
    }
}