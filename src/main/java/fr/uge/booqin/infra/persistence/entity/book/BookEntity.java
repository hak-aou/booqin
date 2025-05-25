package fr.uge.booqin.infra.persistence.entity.book;

import fr.uge.booqin.domain.model.books.ImageFormatModel;
import fr.uge.booqin.infra.persistence.entity.collection.standard.CollectionEntity;
import fr.uge.booqin.infra.persistence.entity.comment.Commentable;
import fr.uge.booqin.infra.persistence.entity.comment.CommentableEntity;
import fr.uge.booqin.infra.persistence.entity.follow.Followable;
import fr.uge.booqin.infra.persistence.entity.follow.FollowableEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.loan.BookSupplyAndDemandEntity;
import fr.uge.booqin.infra.persistence.entity.vote.Votable;
import fr.uge.booqin.infra.persistence.entity.vote.VotableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "books")
public class BookEntity implements Followable, Votable, Commentable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    @Column(name = "isbn13")
    private String isbn13;

    @Column(name = "isbn10")
    private String isbn10;

    @NotNull
    @Column(name = "title")
    private String title;

    @Column(name = "authors")
    @ManyToMany
    private Set<AuthorEntity> authors = new HashSet<>();

    @Column(name = "publishers")
    @ManyToMany
    private Set<PublisherEntity> publishers;

    @Column(name = "published_date")
    private LocalDate publishedDate;

    @ManyToMany
    @Column(name = "categories")
    private Set<CategoryEntity> categories = new HashSet<>();

    @ManyToMany(mappedBy = "books")
    private Set<CollectionEntity> collections = new HashSet<>();

    @ManyToMany(mappedBy = "books")
    private Set<SmartCollectionEntity> smartCollection = new HashSet<>();

    @ManyToOne
    @JoinColumn(name = "language_id")
    private LanguageEntity language;

    @Embedded
    private ImageFormatModel imageLinks;

    @Column(name = "subtitle")
    String subtitle;

    @Column(name = "description", length = 10000) // col TEXT
    private String description;

    @Column(name = "page_count")
    private Integer pageCount;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, mappedBy = "book")
    private BookSupplyAndDemandEntity bookSupplyAndDemand;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "votable_id")
    private VotableEntity votable = new VotableEntity();

    @Column(name = "added_at")
    private Instant addedAt;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "commentable_id", unique = true)
    private CommentableEntity commentable;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "followable_id", unique = true)
    private FollowableEntity followable;

    @PrePersist
    public void prePersist() {
        if (votable == null) {
            votable = new VotableEntity();
        }
        if (commentable == null) {
            commentable = new CommentableEntity();
        }
        if (followable == null) {
            followable = new FollowableEntity();
        }
        if(bookSupplyAndDemand == null) {
            bookSupplyAndDemand = new BookSupplyAndDemandEntity();
            bookSupplyAndDemand.setBook(this);
        }
    }

    public BookEntity() {
    }

    public BookEntity(
            String isbn_13,
            String isbn_10,
            String title,
            List<AuthorEntity> authors,
            List<PublisherEntity> publishers,
            LocalDate publishedDate,
            List<CategoryEntity> categories,
            LanguageEntity language,
            ImageFormatModel imageLinks,
            String subtitle,
            String description,
            Integer pageCount
    ) {
        Objects.requireNonNull(isbn_13);
        Objects.requireNonNull(title);

        this.isbn13 = isbn_13;
        this.isbn10 = isbn_10;
        this.title = title;
        this.authors = new HashSet<>(authors);
        this.publishers = new HashSet<>(publishers);
        this.publishedDate = publishedDate;
        this.categories = new HashSet<>(categories);
        this.language = language;
        this.imageLinks = imageLinks;
        this.subtitle = subtitle;
        this.description = description;
        this.pageCount = pageCount;
    }

    public void setSmartCollection(Set<SmartCollectionEntity> smartCollection) {
        this.smartCollection = smartCollection;
    }

    public Set<SmartCollectionEntity> getSmartCollection() {
        return smartCollection;
    }

    public String getIsbn13() {
        return isbn13;
    }

    public void setIsbn13(String isbn13) {
        this.isbn13 = isbn13;
    }

    public String getIsbn10() {
        return isbn10;
    }

    public void setIsbn10(String isbn10) {
        this.isbn10 = isbn10;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Set<AuthorEntity> getAuthors() {
        return authors;
    }

    public void setAuthors(Set<AuthorEntity> authors) {
        this.authors = authors;
    }

    public Set<PublisherEntity> getPublishers() {
        return publishers;
    }

    public void setPublishers(Set<PublisherEntity> publishers) {
        this.publishers = publishers;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Set<CategoryEntity> getCategories() {
        return categories;
    }

    public void setCategories(Set<CategoryEntity> categories) {
        this.categories = categories;
    }

    public LanguageEntity getLanguage() {
        return language;
    }

    public void setLanguage(LanguageEntity language) {
        this.language = language;
    }

    public ImageFormatModel getImageLinks() {
        return imageLinks;
    }

    public void setImageLinks(ImageFormatModel imageLinks) {
        this.imageLinks = imageLinks;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public VotableEntity getVotable() {
        return votable;
    }

    public Set<CollectionEntity> getCollections() {
        return collections;
    }

    public void setCollections(Set<CollectionEntity> collections) {
        this.collections = collections;
    }

    public void setAddedAt(Instant addedAt) {
        this.addedAt = addedAt;
    }

    public Instant getAddedAt() {
        return addedAt;
    }


    @Override
    public CommentableEntity getCommentable() {
        return commentable;
    }

    @Override
    public FollowableEntity getFollowable() {
        return followable;
    }

    public void addCollection(CollectionEntity collectionEntity) {
        collections.add(collectionEntity);
    }
}
