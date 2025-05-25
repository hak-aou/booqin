package fr.uge.booqin.infra.persistence.repository.collection;

import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface SmartCollectionRepository extends JpaRepository<SmartCollectionEntity, Long> {

    @Query("SELECT f " +
            "FROM SmartCollectionFilterEntity f " +
            "WHERE f.smartCollection.id = :id")
    Set<SmartCollectionFilterEntity> findFiltersByCollectionId(@Param("id") Long id);

}
