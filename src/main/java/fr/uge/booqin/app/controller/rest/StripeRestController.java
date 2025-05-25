package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.infra.external.payment.enums.ChargeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/stripe")
public class StripeRestController {

    @Value("${stripe.api.key}")
    private String stripePublicKey;

    @GetMapping("/checkout")
    public Map<String, Object> checkoutWithStripe() {
        Map<String, Object> response = new HashMap<>();
        response.put("amount", 50 * 100); // In cents
        response.put("stripePublicKey", stripePublicKey);
        response.put("currency", ChargeRequest.Currency.EUR.name());
        return response;
    }
}