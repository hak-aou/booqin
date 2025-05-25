package fr.uge.booqin.infra.persistence.entity.notification;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("FOLLOW")
public class FollowNotificationEntity extends NotificationEntity {
    @ManyToOne
    @JoinColumn(name = "follower_id") // eager by default, and we want that, we always need the user
    private UserEntity follower;

    public UserEntity getFollower() {
        return follower;
    }

    public void setFollower(UserEntity follower) {
        this.follower = follower;
    }

}