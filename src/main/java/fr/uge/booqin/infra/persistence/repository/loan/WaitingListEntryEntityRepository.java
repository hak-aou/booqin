package fr.uge.booqin.infra.persistence.repository.loan;

import fr.uge.booqin.infra.persistence.entity.loan.WaitingListEntryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WaitingListEntryEntityRepository extends JpaRepository<WaitingListEntryEntity, Long> {

    @Query("SELECT w " +
            "FROM WaitingListEntryEntity w " +
            "JOIN FETCH w.bookSupplyAndDemandEntity o " +
            "JOIN FETCH o.book b " +
            "WHERE w.user.id = :userId")
    List<WaitingListEntryEntity> findAllByUserIdInWaitingQueue(UUID userId);

    @Query("SELECT w " +
            "FROM WaitingListEntryEntity w " +
            "JOIN w.bookSupplyAndDemandEntity o " +
            "JOIN o.book b " +
            "WHERE w.user.id = :userId AND b.id = :bookId")
    Optional<WaitingListEntryEntity> findByUserIdAndBookId(UUID userId, UUID bookId);
}
