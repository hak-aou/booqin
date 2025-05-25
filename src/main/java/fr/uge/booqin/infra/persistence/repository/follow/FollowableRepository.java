package fr.uge.booqin.infra.persistence.repository.follow;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.entity.follow.FollowEntity;
import fr.uge.booqin.infra.persistence.entity.follow.FollowableEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FollowableRepository extends JpaRepository<FollowableEntity, UUID>, WithEntityManager {

    @Query("SELECT f " +
            "FROM FollowableEntity f " +
            "LEFT JOIN FETCH f.followers " +
            "WHERE f.id = :id")
    Optional<FollowableEntity> findByIdWithFollowers(UUID id);

    default void follow(UUID userId, UUID followableId) {
        inTransaction(em -> {
            var followableEntity = em.find(FollowableEntity.class, followableId);
            var user = em.find(UserEntity.class, userId);
            var userFollow = new FollowEntity();

            userFollow.setUser(user);
            userFollow.setFollowing(followableEntity);

            em.persist(userFollow);
        });
    }

}