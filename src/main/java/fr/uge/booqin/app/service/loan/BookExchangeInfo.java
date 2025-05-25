package fr.uge.booqin.app.service.loan;

import java.util.UUID;

public record BookExchangeInfo(UUID bookId,
                               int supply,
                               int demand,
                               boolean isBorrowed,
                               boolean isLent,
                               boolean isInCart,
                               InTx inTx

    ) {

    public record InTx(UUID orderId, UUID txId) {}

    @Override
    public String toString() {
        return "BookExchangeInfo{" +
                "bookId=" + bookId +
                ", supply=" + supply +
                ", demand=" + demand +
                ", alreadyBorrowed=" + isBorrowed +
                ", alreadyLent=" + isLent +
                ", isInCart=" + isInCart +
                '}';
    }
}