package fr.uge.booqin.infra.persistence.repository.book;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import org.hibernate.annotations.BatchSize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<BookEntity, UUID>, JpaSpecificationExecutor<BookEntity> {

    Optional<BookEntity> findBooksByIsbn13(String isbn13);

    @Query("SELECT b " +
            "FROM BookEntity b " +
            "JOIN b.collections c " +
            "WHERE c.id = :collectionId")
    @BatchSize(size = 10)
    Page<BookEntity> findAllByCollections_Id(Long collectionId, Pageable page);

    @Query("SELECT b " +
            "FROM BookEntity b " +
            "WHERE LOWER(b.title) LIKE %:title%")
    Page<BookEntity> findBookEntitiesByTitle(String title, Pageable pageable);

    @Query("SELECT b " +
            "FROM BookEntity b " +
            "WHERE b.addedAt > :addedAt")
    List<BookEntity> findBookEntitiesByAddedAtAfter(Instant addedAt);

    Optional<BookEntity> findBookEntityByVotable_Id(UUID votableId);

    @Query("SELECT b " +
            "FROM BookEntity b " +
            "JOIN b.authors a " +
            "WHERE a.name = :name")
    List<BookEntity> findBookEntitiesByAuthors_Name(String name);
}
