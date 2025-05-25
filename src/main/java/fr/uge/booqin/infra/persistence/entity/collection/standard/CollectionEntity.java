package fr.uge.booqin.infra.persistence.entity.collection.standard;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.comment.Commentable;
import fr.uge.booqin.infra.persistence.entity.comment.CommentableEntity;
import fr.uge.booqin.infra.persistence.entity.follow.Followable;
import fr.uge.booqin.infra.persistence.entity.follow.FollowableEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.*;

@Entity
@Table(name = "user_collection")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public class CollectionEntity implements Followable, Commentable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String title;
    private String description;
    private Boolean visibility;

    @ManyToOne(fetch = FetchType.EAGER)
    private UserEntity user;

    @ManyToMany
    @JoinTable(name = "user_collection_books",
            joinColumns = @JoinColumn(name = "collection_id"),
            inverseJoinColumns = @JoinColumn(name = "id"))
    @BatchSize(size = 20)
    private List<BookEntity> books = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "commentable_id", unique = true)
    private CommentableEntity commentable;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "followable_id", unique = true)
    private FollowableEntity followable;

    public CollectionEntity(String title, String description, boolean visibility, UserEntity user) {
        this.title = Objects.requireNonNull(title);
        this.description = Objects.requireNonNull(description);
        this.visibility = visibility;
        this.user = Objects.requireNonNull(user);
    }

    public CollectionEntity() {}

    @PrePersist
    public void prePersist() {
        if (commentable == null) {
            commentable = new CommentableEntity();
        }
        if (followable == null) {
            followable = new FollowableEntity();
        }
    }

    @Override
    public String toString() {
        return "UserCollectionEntity{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", visibility=" + visibility +
                ", user=" + user +
                ", books=" + books +
                '}';
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setVisibility(boolean visibility) {
        this.visibility = visibility;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public void setBooks(List<BookEntity> books) {
        this.books = books;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public boolean isVisible() {
        return visibility;
    }

    public void setVisibility(Boolean visibility) {
        this.visibility = visibility;
    }

    public Boolean getVisibility() {
        return visibility;
    }

    public UserEntity getUser() {
        return user;
    }

    public List<BookEntity> getBooks() {
        return books;
    }

    public void addBook(BookEntity book) {
        books.add(book);
        book.addCollection(this);
    }

    public void addAllBooks(List<BookEntity> books) {
        this.books.addAll(books);
        books.forEach(book -> book.addCollection(this));
    }

    public void removeBook(BookEntity book) {
        books.remove(book);
    }

    public Integer getBookCount() {
        return books.size();
    }

    @Override
    public CommentableEntity getCommentable() {
        return commentable;
    }

    @Override
    public FollowableEntity getFollowable() {
        return followable;
    }

    public void removeAllBooks() {
        books.clear();
    }
}
