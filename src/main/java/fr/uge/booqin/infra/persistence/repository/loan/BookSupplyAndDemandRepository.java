package fr.uge.booqin.infra.persistence.repository.loan;

import fr.uge.booqin.infra.persistence.entity.loan.BookSupplyAndDemandEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookSupplyAndDemandRepository extends JpaRepository<BookSupplyAndDemandEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM BookSupplyAndDemandEntity o JOIN FETCH o.book b WHERE b.id = :bookId")
    Optional<BookSupplyAndDemandEntity> findByBookId(UUID bookId);


    @Query("SELECT o FROM BookSupplyAndDemandEntity o JOIN FETCH o.book b JOIN FETCH o.waitingQueue q WHERE b.id = :bookId AND q.user.id = :userId")
    Optional<Object> findByBookAndUserIdInWaitingQueue(UUID bookId, UUID userId);


    // @Todo: do batch + lazy loading for queue
    @Query("SELECT o FROM BookSupplyAndDemandEntity o " +
            "JOIN FETCH o.book b JOIN FETCH o.waitingQueue q " +
            "WHERE SIZE(o.waitingQueue) > 0")
    List<BookSupplyAndDemandEntity> findAllWithwaitingQueueNotEmpty();
}
