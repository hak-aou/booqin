package fr.uge.booqin.app.dto.follow;

import java.time.Instant;

public record FollowRelationshipDTO(boolean following, Instant followedAt) {
}
