package fr.uge.booqin.app.dto.cart;

import fr.uge.booqin.domain.model.cart.TransactionStepType;

import java.time.Instant;

public record TransactionStep(Long id,
                              TransactionStepType type,
                              Instant date) {

    }
