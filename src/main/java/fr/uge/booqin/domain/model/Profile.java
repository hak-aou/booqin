package fr.uge.booqin.domain.model;

import java.time.Instant;
import java.util.UUID;

public record Profile(UUID id, String username, Instant creationDate, String imageUrl, int numberOfFollowers) {

}
