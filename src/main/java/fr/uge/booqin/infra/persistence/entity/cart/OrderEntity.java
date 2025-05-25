package fr.uge.booqin.infra.persistence.entity.cart;

import fr.uge.booqin.domain.model.cart.OrderStatus;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
public class OrderEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    private UserEntity user;

    private Long cartVersion;

    private Instant creationDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private String paymentType;
    private String paymentTxId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BookTransactionEntity> bookTransactions;

    private Double amount;

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

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public List<BookTransactionEntity> getBookTransactions() {
        return bookTransactions;
    }

    public void setBookTransactions(List<BookTransactionEntity> bookTransactions) {
        this.bookTransactions = bookTransactions;
        for (BookTransactionEntity bookTransaction : bookTransactions) {
            bookTransaction.setOrder(this);
        }
    }

    public Long getCartVersion() {
        return cartVersion;
    }

    public void setCartVersion(Long cartVersion) {
        this.cartVersion = cartVersion;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public String getPaymentTxId() {
        return paymentTxId;
    }

    public void setPaymentTxId(String paymentTransactionId) {
        this.paymentTxId = paymentTransactionId;
    }

}
