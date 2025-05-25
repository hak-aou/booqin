package fr.uge.booqin.infra.persistence.entity.vote;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "votable")
public class VotableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "voter", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<VoteEntity> votes = new HashSet<>();

    @Version
    private Long version;

    private int voteCount = 0;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Set<VoteEntity> getVotes() {
        return votes;
    }

    public void setVotes(Set<VoteEntity> votes) {
        this.votes = votes;
    }

    public int getVoteCount() {
        return voteCount;
    }

    public void setVoteCount(int voteCount) {
        this.voteCount = voteCount;
    }

    public VotableEntity addVote(VoteEntity voteEntity) {
        votes.add(voteEntity);

        var voteType = voteEntity.getVoteType();
        voteCount += voteType.getValue();

        return this;
    }

    public VotableEntity removeVote(VoteType oldVoteType, VoteEntity voteEntity) {
        votes.remove(voteEntity);

        voteCount -= oldVoteType.getValue();

        return this;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }
}