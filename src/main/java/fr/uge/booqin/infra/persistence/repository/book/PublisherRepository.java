package fr.uge.booqin.infra.persistence.repository.book;

import fr.uge.booqin.infra.persistence.entity.book.PublisherEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PublisherRepository extends JpaRepository<PublisherEntity, Long> {

    @Query("SELECT p FROM PublisherEntity p WHERE p.publisherName = :name")
    Optional<PublisherEntity> findByName(String name);
}
