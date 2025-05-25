package fr.uge.booqin.app.dto.collection.smart;

import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;

import java.util.UUID;

public record SmartCollectionInfoDTO(
        Long id,
        UUID commentableId,
        UUID followableId,
        String title,
        String description,
        Boolean visibility,
        Integer bookCount,
        PublicProfileDTO owner,
        FilterBooksDTO filterBooksDTO
) {
}
