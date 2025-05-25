package fr.uge.booqin.infra.persistence.entity.loan;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class WaitingListEntryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "book_loan_offers_id")
    private BookSupplyAndDemandEntity bookSupplyAndDemandEntity;

    private Instant timestamp;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public BookSupplyAndDemandEntity getBookLoanOffersEntity() {
        return bookSupplyAndDemandEntity;
    }

    public void setBookLoanOffersEntity(BookSupplyAndDemandEntity bookSupplyAndDemandEntity) {
        this.bookSupplyAndDemandEntity = bookSupplyAndDemandEntity;
    }


}