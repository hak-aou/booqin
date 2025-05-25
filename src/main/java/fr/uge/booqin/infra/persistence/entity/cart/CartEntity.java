package fr.uge.booqin.infra.persistence.entity.cart;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.BatchSize;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
public class CartEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(mappedBy = "cart")
    private UserEntity owner;

    @ManyToMany
    @JoinTable(name = "user_basket_books",
            joinColumns = @JoinColumn(name = "basket_id"),
            inverseJoinColumns = @JoinColumn(name = "book_id"))
    @BatchSize(size = 10)
    private Set<BookEntity> books = new HashSet<>();

    private Double estimatedPrice = 0.0;

    @Version
    private Long version;

    public CartEntity() {}

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getOwner() {
        return owner;
    }

    public void setOwner(UserEntity owner) {
        this.owner = owner;
    }

    public Set<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(Set<BookEntity> books) {
        this.books = books;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public void addBook(BookEntity book) {
        books.add(book);
    }

    public void removeBook(BookEntity book) {
        books.remove(book);
    }

    public void clear() {
        books.clear();
        estimatedPrice = 0.0;
    }

    public Double getEstimatedPrice() {
        return estimatedPrice;
    }

    public void setEstimatedPrice(Double estimatedPrice) {
        this.estimatedPrice = estimatedPrice;
    }

}
