package fr.uge.booqin.infra.persistence.entity.cart;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
public class BookTransactionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private UserEntity bookOwner;

    @ManyToOne(fetch = FetchType.LAZY)
    private OrderEntity order;

    @ManyToMany
    private Set<BookEntity> books = new HashSet<>();
    private Double amount;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    @Fetch(FetchMode.JOIN)
    private List<TransactionStepEntity> steps;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getBookOwner() {
        return bookOwner;
    }

    public void setBookOwner(UserEntity bookOwner) {
        this.bookOwner = bookOwner;
    }

    public Set<BookEntity> getBooks() {
        return books;
    }

    public void setBooks(Set<BookEntity> books) {
        this.books = books;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public void addBook(BookEntity book) {
        books.add(book);
    }

    public List<TransactionStepEntity> getSteps() {
        return steps;
    }

    public void setSteps(List<TransactionStepEntity> steps) {
        this.steps = steps;
    }

    public void addStep(TransactionStepEntity step) {
        steps.add(step);
    }

    public void removeStep(TransactionStepEntity step) {
        steps.remove(step);
    }

    public OrderEntity getOrder() {
        return order;
    }

    public void setOrder(OrderEntity order) {
        this.order = order;
    }
}
