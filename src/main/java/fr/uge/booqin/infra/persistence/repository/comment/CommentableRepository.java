package fr.uge.booqin.infra.persistence.repository.comment;

import fr.uge.booqin.infra.persistence.entity.comment.CommentableEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CommentableRepository extends JpaRepository<CommentableEntity, UUID> {

}
