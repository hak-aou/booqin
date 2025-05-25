package fr.uge.booqin.infra.persistence.entity.comment;

import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.*;

@Entity
@Table(name = "commentable")
public class CommentableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToMany(mappedBy = "commentable", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @OrderBy("createdAt DESC")
    private List<CommentEntity> comments = new ArrayList<>();

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public List<CommentEntity> getComments() {
        return comments;
    }

    public void setComments(List<CommentEntity> comments) {
        this.comments = comments;
    }


    public void addComment(CommentEntity comment) {
        comments.add(comment);
        comment.setCommentable(this);
    }
}
