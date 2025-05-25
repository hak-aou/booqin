package fr.uge.booqin.domain.model.notification;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.UUID;

public record FollowNotification(
        @JsonUnwrapped
        NotificationBase base,
        UUID followerId,
        String username,
        String avatar) implements Notification {

}

