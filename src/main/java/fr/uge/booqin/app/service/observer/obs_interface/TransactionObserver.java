package fr.uge.booqin.app.service.observer.obs_interface;

import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.cart.TransactionStepType;

import java.util.Optional;
import java.util.UUID;

public interface TransactionObserver {
    void transactionUpdated(User origin, User target, Optional<UUID> orderId, UUID transactionId, TransactionStepType step);
}
