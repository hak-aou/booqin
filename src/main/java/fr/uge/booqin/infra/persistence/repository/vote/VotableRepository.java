package fr.uge.booqin.infra.persistence.repository.vote;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.entity.vote.VotableEntity;
import fr.uge.booqin.infra.persistence.entity.vote.VoteEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface VotableRepository extends JpaRepository<VotableEntity, UUID>, WithEntityManager {

    @Query("SELECT f " +
            "FROM VotableEntity f " +
            "LEFT JOIN FETCH f.votes " +
            "WHERE f.id = :id")
    Optional<VotableEntity> findByIdWithVotes(UUID id);

    default void vote(UUID userId, UUID votableId) {
        inTransaction(em -> {
            var votableEntity = em.find(VotableEntity.class, votableId);
            var user = em.find(UserEntity.class, userId);
            var userVote = new VoteEntity();

            userVote.setUser(user);
            userVote.setVote(votableEntity);

            em.persist(userVote);
        });
    }

}