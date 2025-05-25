package fr.uge.booqin.infra.persistence.entity.loan;

import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
public class LoanOfferEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private UserEntity user; // the user who lend the book

    @ManyToOne
    private BookEntity book; // the book that is lent

    @Column(name = "quantity")
    private int quantity; // the quantity of the book that is lent

    @Version
    private Long version;

    public LoanOfferEntity() {
    }

    public LoanOfferEntity(UserEntity user, BookEntity book, int quantity) {
        this.user = user;
        this.book = book;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

}

// peut etre faire une autre classe héritée de LendingEntity pour le prêt effectif et sont suivi
// puis un fois que le prêt est terminé on re merge LendingEntity
/*

    ex : LE(book x, qtt 2):
            borrowed -> LendingStatus(LE(book x, qtt 1))       .. LE(book x, qtt 1)
            borrowed -> LendingStatus(LE(book x, qtt 1))       .. LE(book x, qtt 0)
            ...both returned ->                                   LE(book x, qtt 2)
 */
