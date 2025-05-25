package fr.uge.booqin.app.dto.cart;

public record CheckoutDTO(
            boolean error,
            String errorMessage,
            Order order
    ){}
