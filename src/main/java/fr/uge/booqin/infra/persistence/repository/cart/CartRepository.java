package fr.uge.booqin.infra.persistence.repository.cart;

import fr.uge.booqin.infra.persistence.entity.cart.CartEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CartRepository extends JpaRepository<CartEntity, UUID>, WithEntityManager {

    default Optional<CartEntity> findByOwnerId(UUID ownerId, LockModeType lockModeType) {
        return inTransaction(tm -> {
            var query = tm.createQuery("SELECT c FROM CartEntity c JOIN FETCH c.owner o WHERE o.id = :ownerId", CartEntity.class);
            query.setParameter("ownerId", ownerId);
            query.setLockMode(lockModeType);
            return query.getResultStream().findFirst();
        });
    }

    default Optional<CartEntity> findByOwnerIdWithBookId(UUID ownerId, UUID bookId, LockModeType lockModeType) {
        return inTransaction(tm -> {
            var query = tm.createQuery("SELECT c FROM CartEntity c JOIN FETCH c.owner o JOIN FETCH c.books b WHERE o.id = :ownerId AND b.id = :bookId", CartEntity.class);
            query.setParameter("ownerId", ownerId);
            query.setParameter("bookId", bookId);
            query.setLockMode(lockModeType);
            return query.getResultStream().findFirst();
        });
    }
}
