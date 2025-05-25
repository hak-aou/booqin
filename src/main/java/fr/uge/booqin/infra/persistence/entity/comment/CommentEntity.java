package fr.uge.booqin.infra.persistence.entity.comment;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.entity.vote.VotableEntity;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import org.hibernate.annotations.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comment",
        indexes = {
                // pagination and sorting
                @Index(name = "idx_comment_commentable_created", columnList = "commentable_id,created_at DESC"),
                // to edit a comment
                @Index(name = "idx_comment_author", columnList = "user_id"),
                // to load replies
                @Index(name = "idx_comment_parent", columnList = "parent_id"),
        }
)
public class CommentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "commentable_id")
    private CommentableEntity commentable;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity author;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(columnDefinition = "TEXT")
    private String content;

    @ManyToOne
    private CommentEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    private List<CommentEntity> replies = new ArrayList<>();

    private int repliesCount = 0;

    @Version
    private Long version;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "votable_id")
    private VotableEntity votable = new VotableEntity();


    public CommentEntity() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public CommentableEntity getCommentable() {
        return commentable;
    }

    public void setCommentable(CommentableEntity commentable) {
        this.commentable = commentable;
    }

    public UserEntity getAuthor() {
        return author;
    }

    public void setAuthor(UserEntity author) {
        this.author = author;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public CommentEntity getParent() {
        return parent;
    }

    public void setParent(CommentEntity parent) {
        this.parent = parent;
    }

    public List<CommentEntity> getReplies() {
        return replies;
    }

    public void setReplies(List<CommentEntity> replies) {
        this.replies = replies;
    }

    public void setVotable(VotableEntity votable) {
        this.votable = votable;
    }

    public VotableEntity getVotable() {
        return votable;
    }

    public CommentEntity reply(CommentEntity reply) {
        reply.setParent(this);
        replies.add(reply);
        repliesCount++;
        return this;
    }

    public CommentEntity removeReply(CommentEntity comment) {
        replies.remove(comment);
        repliesCount--;
        return comment;
    }

    public int getRepliesCount() {
        return repliesCount;
    }

    public void setRepliesCount(int repliesCount) {
        this.repliesCount = repliesCount;
    }
}
