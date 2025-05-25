package fr.uge.booqin.infra.persistence.entity.notification;

import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@DiscriminatorValue("TX_STEP")
public class TxStepNotificationEntity extends NotificationEntity {
    private String txStepType;
    private UUID txId;
    private UUID orderId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "other_user_id")
    private UserEntity otherUser;

    public String getTxStepType() {
        return txStepType;
    }

    public void setTxStepType(String txStepType) {
        this.txStepType = txStepType;
    }

    public UUID getTxId() {
        return txId;
    }

    public void setTxId(UUID txId) {
        this.txId = txId;
    }

    public UserEntity getOtherUser() {
        return otherUser;
    }

    public void setOtherUser(UserEntity otherUser) {
        this.otherUser = otherUser;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public void setOrderId(UUID orderId) {
        this.orderId = orderId;
    }
}
