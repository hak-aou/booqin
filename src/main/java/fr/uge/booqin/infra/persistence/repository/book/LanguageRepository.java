package fr.uge.booqin.infra.persistence.repository.book;

import fr.uge.booqin.infra.persistence.entity.book.LanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LanguageRepository extends JpaRepository<LanguageEntity, Long> {

    @Query("SELECT l FROM LanguageEntity l WHERE l.languageName = :languageName")
    Optional<LanguageEntity> findByLanguageName(String languageName);

}
