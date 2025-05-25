package fr.uge.booqin.infra.persistence.repository.loan;

import fr.uge.booqin.infra.persistence.entity.loan.LoanOfferEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanOfferRepository extends JpaRepository<LoanOfferEntity, UUID>, WithEntityManager {

    default Optional<LoanOfferEntity> findByBookId(UUID bookId, LockModeType lockModeType) {
        return inTransaction(tm -> {
            var query = tm.createQuery("SELECT o FROM LoanOfferEntity o JOIN FETCH o.book b WHERE b.id = :bookId", LoanOfferEntity.class);
            query.setParameter("bookId", bookId);
            query.setLockMode(lockModeType);
            return query.getResultStream().findFirst();
        });
    }

    default Optional<LoanOfferEntity> findByBookIdAndOwnerId(UUID ownerId, UUID bookId, LockModeType lockModeType) {
        return inTransaction(tm -> {
            var query = tm.createQuery("SELECT o FROM LoanOfferEntity o JOIN FETCH o.book b WHERE b.id = :bookId AND o.user.id = :ownerId", LoanOfferEntity.class);
            query.setParameter("bookId", bookId);
            query.setParameter("ownerId", ownerId);
            query.setLockMode(lockModeType);
            return query.getResultStream().findFirst();
        });
    }

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM LoanOfferEntity o JOIN FETCH o.book b WHERE o.user.id = :id")
    List<LoanOfferEntity> findByOwnerId(UUID id);
}
