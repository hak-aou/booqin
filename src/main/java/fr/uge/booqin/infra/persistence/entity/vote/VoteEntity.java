package fr.uge.booqin.infra.persistence.entity.vote;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "vote",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "votable_id"})
)
public class VoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "votable_id")
    private VotableEntity voter;

    @Column(name = "vote_type")
    private VoteType voteType;

    private Instant votedAt;

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

    public VotableEntity getVote() {
        return voter;
    }

    public void setVote(VotableEntity votableEntity) {
        this.voter = votableEntity;
    }

    public void removeVote() {
        this.voter.removeVote(voteType, this);
        this.voter = null;
    }

    public Instant getVotedAt() {
        return votedAt;
    }

    public void setVotedAt(Instant votedAt) {
        this.votedAt = votedAt;
    }

    public VoteType getVoteType() {
        return voteType;
    }

    public void setVoteType(VoteType voteType) {
        this.voteType = voteType;
    }
}
