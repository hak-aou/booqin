package fr.uge.booqin.infra.persistence.repository.notification;

import fr.uge.booqin.infra.persistence.entity.notification.NotificationEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, UUID>, WithEntityManager {

    @Query("SELECT n FROM NotificationEntity n JOIN n.recipient u WHERE u.id = :userId ORDER BY n.createdAt DESC")
    @BatchSize(size = 10)
    Page<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);

}
