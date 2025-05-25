package fr.uge.booqin.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

public record InfoNotification(
        @JsonUnwrapped
        NotificationBase base,
        String message) implements Notification{
}