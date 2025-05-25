package fr.uge.booqin.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.UUID;

public record TxStepUpdateNotification(
        @JsonUnwrapped
        NotificationBase base,
        UUID otherUserId,
        String username,
        String avatar,
        UUID orderId,
        UUID txId,
        String stepType) implements Notification {
}
