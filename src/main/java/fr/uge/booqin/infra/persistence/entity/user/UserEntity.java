package fr.uge.booqin.infra.persistence.entity.user;

import fr.uge.booqin.infra.persistence.entity.follow.FollowEntity;
import fr.uge.booqin.infra.persistence.entity.follow.Followable;
import fr.uge.booqin.infra.persistence.entity.follow.FollowableEntity;
import fr.uge.booqin.infra.persistence.entity.loan.LoanOfferEntity;
import fr.uge.booqin.infra.persistence.entity.cart.CartEntity;
import fr.uge.booqin.infra.persistence.entity.notification.UserNotificationsEntity;
import fr.uge.booqin.infra.persistence.entity.vote.VoteEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;


@Entity
@Table(name = "user")
public class UserEntity implements Followable {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true)
    private String username;

    @Column(unique = true)
    private String email;

    private Instant creationDate;

    @Column(columnDefinition = "text")
    private String imageUrl;

    @Column(unique = true)
    private UUID authIdentityId;

    @Column(columnDefinition = "boolean default false")
    private Boolean isAdmin;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "followable_id", unique = true)
    private FollowableEntity followable;

    @OneToMany(mappedBy = "user")
    private final Set<FollowEntity> following = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<VoteEntity> votes = new HashSet<>();

    @OneToMany(mappedBy = "user")
    private Set<LoanOfferEntity> bookLoanOffers = new HashSet<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private UserNotificationsEntity notifications;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    private CartEntity cart;

    @PrePersist
    public void prePersist() {
        if (followable == null) {
            followable = new FollowableEntity();
        }
        if(cart == null) {
            cart = new CartEntity();
        }
        if (notifications == null) {
            notifications = new UserNotificationsEntity();
            notifications.setUser(this);
        }
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String picture) {
        this.imageUrl = picture;
    }

    public UUID getAuthIdentityId() {
        return authIdentityId;
    }

    public void setAuthIdentityId(UUID authIdentityId) {
        this.authIdentityId = authIdentityId;
    }

    @Override
    public FollowableEntity getFollowable() {
        return followable;
    }

    public void setFollowable(FollowableEntity followable) {
        this.followable = followable;
    }

    public Set<FollowEntity> getFollowing() {
        return following;
    }


    public void setVotes(Set<VoteEntity> votes) {
        this.votes = votes;
    }

    public Set<VoteEntity> getVotes() {
        return votes;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean isAdmin) {
        this.isAdmin = isAdmin;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public UserNotificationsEntity getNotifications() {
        return notifications;
    }

    public void setNotifications(UserNotificationsEntity notifications) {
        this.notifications = notifications;
    }
}
