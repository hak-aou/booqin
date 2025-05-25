package fr.uge.booqin.infra.persistence.repository.cart;

import fr.uge.booqin.domain.model.cart.OrderStatus;
import fr.uge.booqin.infra.persistence.entity.cart.OrderEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, WithEntityManager {

    @Query("SELECT o FROM OrderEntity o JOIN FETCH o.user u WHERE u.id = :id AND o.status = :orderStatus")
    List<OrderEntity> findByUserIdAndStatus(UUID id, OrderStatus orderStatus);

    default Optional<OrderEntity> findById(UUID id, LockModeType lockModeType) {
        return inTransaction(tm -> {
            var query = tm.find(OrderEntity.class, id, lockModeType);
            return Optional.ofNullable(query);
        });
    }
}
