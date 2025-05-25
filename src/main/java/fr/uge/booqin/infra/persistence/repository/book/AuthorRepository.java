package fr.uge.booqin.infra.persistence.repository.book;

import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface AuthorRepository extends JpaRepository<AuthorEntity, Long> {

    @Query("SELECT a FROM AuthorEntity a WHERE a.name = :s")
    Optional<AuthorEntity> findByName(String s);
}
