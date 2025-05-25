package fr.uge.booqin.app.dto.cart;

import com.stripe.model.PaymentIntent;

public record PaymentIntentDTO(String id, String clientSecret, String status, boolean error, String errorMessage) {
    public static PaymentIntentDTO fromIntent(PaymentIntent intent, boolean error, String errorMessage) {
        return new PaymentIntentDTO(
            intent.getId(),
            intent.getClientSecret(),
            intent.getStatus(),
            error,
            errorMessage
        );
    }
    public static PaymentIntentDTO errored(String errorMessage) {
        return new PaymentIntentDTO(null, null, null, true, errorMessage);
    }
}