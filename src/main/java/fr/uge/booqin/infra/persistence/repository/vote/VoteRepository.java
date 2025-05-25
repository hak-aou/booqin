package fr.uge.booqin.infra.persistence.repository.vote;

import fr.uge.booqin.infra.persistence.entity.vote.VoteEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface VoteRepository extends JpaRepository<VoteEntity, UUID>, WithEntityManager {

    @Query("SELECT v " +
            "FROM VoteEntity v " +
            "WHERE v.user.id = :userId AND v.voter.id = :votableId")
    Optional<VoteEntity> findByUser_IdAndVotable_Id(UUID userId, UUID votableId);
}
