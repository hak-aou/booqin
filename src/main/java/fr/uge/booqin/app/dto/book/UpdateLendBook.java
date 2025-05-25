package fr.uge.booqin.app.dto.book;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public record UpdateLendBook(
        UUID bookId,
        @Min(0)
        int quantity
) {
}
