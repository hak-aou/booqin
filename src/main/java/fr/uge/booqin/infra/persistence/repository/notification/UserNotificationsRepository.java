package fr.uge.booqin.infra.persistence.repository.notification;

import fr.uge.booqin.infra.persistence.entity.notification.UserNotificationsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserNotificationsRepository extends JpaRepository<UserNotificationsEntity, Long> {

    @Query("SELECT uc " +
            "FROM UserNotificationsEntity uc " +
            "JOIN uc.user u " +
            "WHERE u.id = :userId")
    Optional<UserNotificationsEntity> findByUserId(UUID userId);
}
