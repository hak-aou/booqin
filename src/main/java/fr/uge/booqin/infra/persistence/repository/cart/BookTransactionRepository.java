package fr.uge.booqin.infra.persistence.repository.cart;

import fr.uge.booqin.domain.model.cart.OrderStatus;
import fr.uge.booqin.domain.model.cart.TransactionStepType;
import fr.uge.booqin.infra.persistence.entity.cart.BookTransactionEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookTransactionRepository extends JpaRepository<BookTransactionEntity, UUID> {

    @Query("SELECT bt FROM BookTransactionEntity bt JOIN bt.order o WHERE bt.bookOwner.id = :bookOwner AND o.status = :status")
    List<BookTransactionEntity> findByBookOwner(UUID bookOwner, OrderStatus status);

    @Query("SELECT bt " +
            "FROM BookTransactionEntity bt " +
            "JOIN FETCH bt.order o " +
            "JOIN FETCH o.user u " +
            "WHERE bt.id = :txId " +
            "AND (bt.bookOwner.id = :userId OR u.id = :userId)"
    )
    Optional<BookTransactionEntity> findByTxIdByBorrowerOrByLender(UUID txId, UUID userId);

    @Query("SELECT bt FROM BookTransactionEntity bt " +
            "JOIN bt.order o " +
            "JOIN o.user u " +
            "JOIN bt.steps s " +
            "JOIN bt.books b " +
            "WHERE (bt.bookOwner.id = :userId OR u.id = :userId) " +
            "AND o.status = 'FULFILLED' " +
            "AND b.id = :bookId " +
            "AND :azea NOT IN (SELECT s.type FROM bt.steps s)"
    )
    List<BookTransactionEntity> findFirstByBookIdAndBorrowerOrLender(UUID bookId, UUID userId, Pageable pageable, TransactionStepType azea);
}
