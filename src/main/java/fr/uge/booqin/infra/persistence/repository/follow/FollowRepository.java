package fr.uge.booqin.infra.persistence.repository.follow;

import fr.uge.booqin.infra.persistence.entity.follow.FollowEntity;
import fr.uge.booqin.infra.persistence.repository.common.WithEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface FollowRepository extends JpaRepository<FollowEntity, UUID>, WithEntityManager {

    @Query("SELECT f FROM FollowEntity f JOIN FETCH f.user JOIN FETCH f.following WHERE f.following.id = :followableId")
    Page<FollowEntity> findAllByFollowing_Id(UUID followableId, Pageable pageable);


    default Optional<FollowEntity> findByUser_IdAndFollowing_Id(UUID userId, UUID followableId) {
        return inTransaction(em -> {
            var q = "SELECT f FROM FollowEntity f " +
                    "JOIN FETCH f.following fol " +
                    "JOIN FETCH f.user u "+
                    "WHERE f.user.id = :userId AND f.following.id = :followableId";
            var query = em.createQuery(q, FollowEntity.class)
                    .setParameter("userId", userId)
                    .setParameter("followableId", followableId);
            return query.getResultStream().findFirst();
        });
    }
}
