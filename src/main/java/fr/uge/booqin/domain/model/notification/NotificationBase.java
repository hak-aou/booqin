package fr.uge.booqin.domain.model.notification;

import java.time.Instant;
import java.util.UUID;

public record NotificationBase(
        UUID id,
        Instant createdAt,
        boolean read) {
}
