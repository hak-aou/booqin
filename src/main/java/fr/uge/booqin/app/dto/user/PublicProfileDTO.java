package fr.uge.booqin.app.dto.user;

import java.time.Instant;
import java.util.UUID;

public record PublicProfileDTO(
        UUID id,
        UUID followableId,
        String username,
        Instant creationDate,
        String imageUrl,
        int numberOfFollowers) {
}
