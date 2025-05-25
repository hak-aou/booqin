package fr.uge.booqin.infra.persistence.entity.cart;


import fr.uge.booqin.domain.model.cart.TransactionStepType;
import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class TransactionStepEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TransactionStepType type;

    private Instant timestamp;

    public TransactionStepEntity() {
    }

    public static TransactionStepEntity firstStep() {
        var step = new TransactionStepEntity();
        step.setType(TransactionStepType.TO_BE_SENT);
        step.setTimestamp(Instant.now());
        return step;
    }

    public static TransactionStepEntity of(TransactionStepType type) {
        var step = new TransactionStepEntity();
        step.setType(type);
        step.setTimestamp(Instant.now());
        return step;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TransactionStepType getType() {
        return type;
    }

    public void setType(TransactionStepType type) {
        this.type = type;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

}
