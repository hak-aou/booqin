package fr.uge.booqin.infra.persistence.entity.notification;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
public class UserNotificationsEntity {
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private UserEntity user;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private List<NotificationEntity> notifications = new ArrayList<>();

    private Integer numberOfUnread = 0;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public List<NotificationEntity> getNotifications() {
        return notifications;
    }

    public void setNotifications(List<NotificationEntity> notifications) {
        this.notifications = notifications;
    }

    public Integer getNumberOfUnread() {
        return numberOfUnread;
    }

    public void setNumberOfUnread(Integer numberOfUnread) {
        this.numberOfUnread = numberOfUnread;
    }

    public void incrementNumberOfUnread() {
        this.numberOfUnread++;
    }

    public void decrementNumberOfUnread() {
        this.numberOfUnread--;
    }

    public void addNotification(NotificationEntity notification) {
        this.notifications.add(notification);
        incrementNumberOfUnread();
    }


    public void removeNotifications(List<UUID> notificationId) {
        this.notifications.removeIf(notification -> notificationId.contains(notification.getId()));
        this.numberOfUnread = (int) this.notifications.stream().filter(notification -> !notification.isRead()).count();
    }
}
