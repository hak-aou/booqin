package fr.uge.booqin.infra.persistence.entity.follow;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "follow",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "followable_id"})
)
public class FollowEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "followable_id")
    private FollowableEntity following;

    private Instant followedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public FollowableEntity getFollowing() {
        return following;
    }

    public void setFollowing(FollowableEntity following) {
        this.following = following;
    }

    public Instant getFollowedAt() {
        return followedAt;
    }

    public void setFollowedAt(Instant followedAt) {
        this.followedAt = followedAt;
    }
}
