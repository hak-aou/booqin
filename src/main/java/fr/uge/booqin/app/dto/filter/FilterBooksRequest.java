package fr.uge.booqin.app.dto.filter;

import fr.uge.booqin.app.dto.pagination.PageRequest;

public record FilterBooksRequest(
        FilterBooksDTO filterBooksDTO,
        PageRequest pageRequest
) { }
