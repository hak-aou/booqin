package fr.uge.booqin.app.dto.collection;

import fr.uge.booqin.app.dto.user.PublicProfileDTO;

import java.util.UUID;

public record CollectionInfoDTO(
        Long id,
        UUID commentableId,
        UUID followableId,
        String title,
        String description,
        Boolean visibility,
        Integer bookCount,
        PublicProfileDTO owner
) {
}
