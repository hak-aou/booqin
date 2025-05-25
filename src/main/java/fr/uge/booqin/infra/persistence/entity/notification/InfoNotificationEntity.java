package fr.uge.booqin.infra.persistence.entity.notification;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("INFO")
public class InfoNotificationEntity extends NotificationEntity {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
