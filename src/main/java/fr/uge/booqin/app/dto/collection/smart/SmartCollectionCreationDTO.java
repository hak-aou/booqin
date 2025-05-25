package fr.uge.booqin.app.dto.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;

public record SmartCollectionCreationDTO(
        String title,
        String description,
        Boolean visibility,
        FilterBooksDTO filterBooksDTO
) {
}
