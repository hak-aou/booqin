package fr.uge.booqin.app.service.vote;

import fr.uge.booqin.app.dto.vote.HasVoteDTO;
    import fr.uge.booqin.app.service.observer.obs_interface.VoteObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.persistence.entity.vote.VoteEntity;
import fr.uge.booqin.infra.persistence.entity.vote.VoteType;
import fr.uge.booqin.infra.persistence.repository.vote.VotableRepository;
import fr.uge.booqin.infra.persistence.repository.vote.VoteRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class VoteServiceWithFailure {

    private final UserRepository userRepository;
    private final VotableRepository votableRepository;
    private final VoteRepository voteRepository;
    private final List<VoteObserver> observers;

    public VoteServiceWithFailure(
            UserRepository userRepository,
            VotableRepository votableRepository,
            VoteRepository voteRepository,
            List<VoteObserver> observers,
            Validator validator) {
        this.userRepository = userRepository;
        this.votableRepository = votableRepository;
        this.voteRepository = voteRepository;
        this.observers = new ArrayList<>(observers);
    }

    private void voteImpl(User user,
                          VoteType voteType,
                          UUID votableId) {
        var votable = votableRepository.findById(votableId)
                .orElseThrow(() -> new TheirFaultException("Votable not found"));
        var userEntity = userRepository.findById(user.id())
                .orElseThrow(() -> new TheirFaultException("User not found"));
        var vote = new VoteEntity();

        vote.setUser(userEntity);
        vote.setVotedAt(Instant.now());
        vote.setVoteType(voteType);
        vote.setVote(votable);

        votable = votable.addVote(vote);
        votableRepository.save(votable);
    }

    @Transactional
    public void vote(User user,
                     VoteType voteType,
                     UUID votableId) {

        voteImpl(user, voteType, votableId);

        observers.forEach(observer ->
                observer.notifyVote(user, votableId));
    }

    @Transactional
    public void unvote(User user,
                       UUID votableId) {
        var votable = votableRepository.findByIdWithVotes(votableId)
                .orElseThrow(() -> new TheirFaultException("Votable not found"));

        var voteE = voteRepository.findByUser_IdAndVotable_Id(user.id(), votableId)
                .orElseThrow(() -> new TheirFaultException("Vote not found"));

        voteRepository.findByUser_IdAndVotable_Id(user.id(), votableId)
                .ifPresent(vote -> votableRepository.save(votable.removeVote(voteE.getVoteType(), vote)));

        observers.forEach(observer ->
                observer.notifyUnvote(user, votableId));
    }


    @Transactional
    public int getVoteCountForBook(UUID votableId) {
        var votable = votableRepository.findById(votableId)
                .orElseThrow(() -> new TheirFaultException("Votable not found"));
        return votable.getVoteCount();
    }


    @Transactional
    public HasVoteDTO hasVotedOnBook(User user,
                                  UUID votableId) {
        var vote = voteRepository.findByUser_IdAndVotable_Id(user.id(), votableId);

        if (vote.isEmpty()) {
            return new HasVoteDTO(false, null, null);
        }

        return new HasVoteDTO(
                true,
                vote.get().getVoteType(),
                vote.get().getVotedAt()
        );
    }
}
