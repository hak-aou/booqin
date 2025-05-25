package fr.uge.booqin.app.dto.cart;

import fr.uge.booqin.domain.model.cart.OrderStatus;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record Order(UUID orderId,
                    UUID userId,
                    Long cartVersion,
                    Instant creationDate,
                    OrderStatus status,
                    Double amount,
                    String paymentType,
                    String paymentTxId,
                    List<BookTransaction> bookTransactions) {

    }

