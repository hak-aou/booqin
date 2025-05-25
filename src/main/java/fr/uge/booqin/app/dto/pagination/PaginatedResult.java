package fr.uge.booqin.app.dto.pagination;

import java.util.List;

public record PaginatedResult<T>(
        List<T> data,
        long totalResults,
        int numberInPage,
        long offset,
        int limit) {
}
