package fr.uge.booqin.app.dto.filter;

import java.time.LocalDate;

public record IntervalDateDTO(
        LocalDate min,
        LocalDate max
) implements FilterDTO{
}