package fr.uge.booqin.infra.persistence.repository.collection;

import fr.uge.booqin.infra.persistence.entity.collection.standard.CollectionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CollectionRepository extends JpaRepository<CollectionEntity, Long> {

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.followable f " +
            "WHERE uc.visibility = :b")
    Page<CollectionEntity> findAllByVisibility(boolean b, Pageable pageable);

    List<CollectionEntity> findAllByVisibility(boolean b);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.user u " +
            "WHERE uc.id = :id " +
            "AND (uc.visibility = true OR u.id = :userId)")
    Optional<CollectionEntity> findByIdVisibleOrOwnedByUser(Long id, UUID userId);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.user u " +
            "WHERE uc.id = :id " +
            "AND u.id = :userId")
    Optional<CollectionEntity> findByIdOwnedByUser(Long id, UUID userId);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.books " +
            "WHERE uc.id = :id")
    Optional<CollectionEntity> findByIdWithBooks(Long id);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.user u " +
            "WHERE u.id = :userId")
    List<CollectionEntity> findByUserId(UUID userId);

    @Query("SELECT uc FROM CollectionEntity uc WHERE uc.id = :id AND uc.visibility = :b")
    Optional<CollectionEntity> findByIdWithVisibility(Long id, boolean b);

    @Query ("SELECT uc FROM CollectionEntity uc WHERE uc.user.id = :userId AND uc.title = :title")
    Optional<CollectionEntity> findByOwnerIdAndTitle(UUID userId, String title);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "WHERE LOWER(uc.title) LIKE LOWER(CONCAT('%', :title, '%'))" +
            "AND uc.visibility = true")
    Page<CollectionEntity> findPublicCollectionEntitiesByTitle(String title, Pageable pageable);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.user c " +
            "JOIN FETCH uc.books b " +
            "WHERE c.id = :id " +
            "AND b.id = :bookId")
    List<CollectionEntity> findByUserIdAndBook(UUID id, UUID bookId);

    @Query("SELECT uc " +
            "FROM CollectionEntity uc " +
            "JOIN FETCH uc.user c " +
            "JOIN FETCH uc.books b " +
            "WHERE c.id = :userId " +
            "AND b.id = :bookId")
    List<CollectionEntity> findAllByUserIdAndVisibility(UUID userId, boolean b);

    @Query("SELECT COUNT(b) FROM CollectionEntity c JOIN c.books b WHERE c.id = :collectionId")
    Long collectionSize(Long collectionId);
}
