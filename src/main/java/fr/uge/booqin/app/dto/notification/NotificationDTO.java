package fr.uge.booqin.app.dto.notification;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import fr.uge.booqin.domain.model.notification.FollowNotification;
import fr.uge.booqin.domain.model.notification.InfoNotification;
import fr.uge.booqin.domain.model.notification.Notification;
import fr.uge.booqin.domain.model.notification.TxStepUpdateNotification;

public record NotificationDTO(
        String type,
        @JsonUnwrapped
        Notification notification
) {
    public static String getNotificationType(Notification notification) {
        return switch (notification) {
            case FollowNotification x -> "FOLLOW";
            case InfoNotification x -> "INFO";
            case TxStepUpdateNotification x -> "TX_STEP";
        };
    }

    public static NotificationDTO from(Notification notif) {
        return new NotificationDTO(getNotificationType(notif), notif);
    }
}
