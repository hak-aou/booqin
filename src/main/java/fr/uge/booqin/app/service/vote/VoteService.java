package fr.uge.booqin.app.service.vote;


import fr.uge.booqin.app.dto.vote.HasVoteDTO;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.persistence.entity.vote.VoteType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class VoteService {

    private final VoteServiceWithFailure voteServiceWithFailure;
    public VoteService(VoteServiceWithFailure voteServiceWithFailure) {
        this.voteServiceWithFailure = voteServiceWithFailure;
    }

    @Transactional
    public void vote(User user,
                     VoteType voteType,
                     UUID votableId) {
        ServiceUtils.optimisticRetry(() -> voteServiceWithFailure.vote(user, voteType, votableId));
    }

    @Transactional
    public void unvote(User user, UUID votableId) {
        ServiceUtils.optimisticRetry(() -> voteServiceWithFailure.unvote(user, votableId));
    }

    @Transactional
    public long getVoteValue(UUID votableId) {
        return ServiceUtils.optimisticRetry(() -> voteServiceWithFailure.getVoteCountForBook(votableId));
    }

    @Transactional
    public HasVoteDTO hasVoted(User user,
                               UUID votableId) {
        return voteServiceWithFailure.hasVotedOnBook(user, votableId);
    }

}
