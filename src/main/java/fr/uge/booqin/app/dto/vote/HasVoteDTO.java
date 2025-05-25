package fr.uge.booqin.app.dto.vote;

import fr.uge.booqin.infra.persistence.entity.vote.VoteType;

import java.time.Instant;

public record HasVoteDTO(boolean hasVoted,
                         VoteType voteType,
                         Instant votedAt) {
}
