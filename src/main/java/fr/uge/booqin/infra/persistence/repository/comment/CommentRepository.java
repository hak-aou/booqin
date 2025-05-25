package fr.uge.booqin.infra.persistence.repository.comment;

import fr.uge.booqin.infra.persistence.entity.comment.CommentEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    @Query("SELECT c " +
            "FROM CommentEntity c " +
            "JOIN FETCH c.replies " +
            "WHERE c.id = :id")
    Optional<CommentEntity> findByIdWithReplies(Long id);

    @Query("SELECT c " +
            "FROM CommentEntity c " +
            "JOIN FETCH c.author a " +
            "WHERE c.id = :id AND a.id = :userId")
    Optional<CommentEntity> findCommentByIdAndAuthor(Long id, UUID userId);

    @Query("SELECT c " +
            "FROM CommentEntity c " +
            "JOIN FETCH c.commentable " +
            "WHERE c.commentable.id = :commentableId")
    Page<CommentEntity> findAllByCommentable_Id(UUID commentableId, Pageable page);

    @Query("SELECT c " +
            "FROM CommentEntity c " +
            "WHERE c.parent.id = :commentId")
    Page<CommentEntity> findReplies(Long commentId, Pageable page);

    @Query("""
        SELECT c
        FROM CommentEntity c
        WHERE c.commentable.id = :commentableId
        AND c.parent IS NULL
        """)
    // LEFT JOIN FETCH c.replies
    Page<CommentEntity> findRootCommentsWithImmediateReplies(UUID commentableId, Pageable pageable);

    // We could add other queries to load comments for when the subject 'hot'
    // with a lot of vote/interactions
    // to load the comments tree with a depth of 2 or 3 levels
}
