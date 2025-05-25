package fr.uge.booqin.infra.persistence.repository.user;

import org.hibernate.annotations.BatchSize;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID>, WithEntityManager {

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.followable f WHERE u.authIdentityId = :authIdentityId")
    Optional<UserEntity> findByAuthIdentityId(UUID authIdentityId);

    @NonNull
    @Query("SELECT u FROM UserEntity u JOIN FETCH u.followable f WHERE u.id = :id")
    Optional<UserEntity> findById(@NonNull UUID id);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.notifications WHERE u.id = :id")
    Optional<UserEntity> findByIdWithNotifications(UUID id);

    Optional<UserEntity> findByUsername(String username);

    ///
    /// Get all users that a userId is following
    ///
    @Query("Select otherUser " +
            "FROM UserEntity otherUser " +
            "JOIN FollowEntity follow ON otherUser.followable.id = follow.following.id " +
            "JOIN UserEntity me ON follow.user.id = me.id " +
            "WHERE me.id = :userId")
    Page<UserEntity> findFollowings (UUID userId, Pageable pageable);

    @Query("Select u " +
            "FROM UserEntity u " +
            "JOIN FETCH u.followable f " +
            "WHERE f.id = :followableIdOfUser")
    Optional<UserEntity> findByFollowableId(UUID followableIdOfUser);

    @Query("SELECT u FROM UserEntity u WHERE LOWER(u.username) LIKe %:username%")
    Page<UserEntity> findUserEntitiesByUsername(String username, Pageable pageable); // TODO : SEARCH

    Optional<UserEntity> findByEmail(String email);

    @Query("SELECT u FROM UserEntity u JOIN FETCH u.notifications ")
    @BatchSize(size = 1000)
    List<UserEntity> findAllWithNotifications();
}
