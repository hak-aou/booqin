package fr.uge.booqin.app.dto.filter;

public record IntervalNumberDTO(
        int min,
        int max
) implements FilterDTO {
}