package fr.uge.booqin.app.dto.pagination;

import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;

@Validated
public record PageRequest(
        @Min(0)
        int offset,
        @Min(1)
        int limit) {
}
