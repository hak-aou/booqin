package fr.uge.booqin.domain.model.books;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class BookModelBuilder {
    private UUID votableId;
    private String title;
    private UUID id;
    private UUID commentableId;
    private UUID followableId;
    private IsbnModel isbn;
    private List<String> authors;

    private List<String> publisher;
    private LocalDate publishedDate;

    private List<String> categories;
    private String language;
    private ImageFormatModel imageLinks;

    private String subtitle;
    private String description;
    private Integer pageCount;

    public BookModelBuilder votableId(UUID votableId) {
        this.votableId = votableId;
        return this;
    }

    public BookModelBuilder title(String title) {
        this.title = Objects.requireNonNull(title);
        return this;
    }

    public BookModelBuilder id(UUID id) {
        this.id = Objects.requireNonNull(id);
        return this;
    }

    public BookModelBuilder commentableId(UUID commentableId) {
        this.commentableId = Objects.requireNonNull(commentableId);
        return this;
    }

    public BookModelBuilder followableId(UUID followableId) {
        this.followableId = Objects.requireNonNull(followableId);
        return this;
    }

    public BookModelBuilder isbn(IsbnModel isbn) {
        this.isbn = Objects.requireNonNull(isbn);
        return this;
    }

    public BookModelBuilder authors(List<String> authors) {
        this.authors = authors == null ? List.of() : List.copyOf(authors);
        return this;
    }

    public BookModelBuilder publisher(List<String> publisher) {
        this.publisher = publisher == null ? List.of() : List.copyOf(publisher);
        return this;
    }

    public BookModelBuilder publisher(String publisher) {
        this.publisher = publisher == null ? List.of() : List.of(publisher);
        return this;
    }

    public BookModelBuilder publishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
        return this;
    }

    public BookModelBuilder categories(List<String> categories) {
        this.categories = categories == null ? List.of() : List.copyOf(categories);
        return this;
    }

    public BookModelBuilder language(String language) {
        this.language = language;
        return this;
    }

    public BookModelBuilder imageLinks(ImageFormatModel imageLinks) {
        this.imageLinks = imageLinks == null ? new ImageFormatModel(null, null, null) : imageLinks;
        return this;
    }

    public BookModelBuilder subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    public BookModelBuilder description(String description) {
        this.description = description;
        return this;
    }

    public BookModelBuilder pageCount(Integer pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    public BookModel build() {
        return new BookModel(
                votableId,
                title,
                id,
                commentableId,
                followableId,
                isbn,
                authors,
                publisher,
                publishedDate,
                categories,
                language,
                imageLinks,
                subtitle,
                description,
                pageCount
        );
    }
}