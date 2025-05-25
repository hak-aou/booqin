package fr.uge.booqin.infra.persistence.entity.follow;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "followable")
public class FollowableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "following", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FollowEntity> followers = new HashSet<>();

    @Version
    private Long version;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Set<FollowEntity> getFollowers() {
        return followers;
    }

    public void setFollowers(Set<FollowEntity> followers) {
        this.followers = followers;
    }

    public FollowableEntity addFollower(FollowEntity followEntity) {
        followers.add(followEntity);
        return this;
    }

    public FollowableEntity removeFollower(FollowEntity followEntity) {
        followers.remove(followEntity);
        return this;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}