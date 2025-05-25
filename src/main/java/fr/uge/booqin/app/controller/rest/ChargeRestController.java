package fr.uge.booqin.app.controller.rest;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import fr.uge.booqin.app.service.StripeService;
import fr.uge.booqin.infra.external.payment.enums.ChargeRequest;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/charge")
public class ChargeRestController {

    private final StripeService stripeService;

    public ChargeRestController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping
    public Map<String, Object> charge(@RequestBody ChargeRequest chargeRequest) throws StripeException {
        System.out.println("Charge Request: " + chargeRequest);
        Map<String, Object> response = new HashMap<>();
        Charge charge = stripeService.charge(chargeRequest);
        response.put("id", charge.getId());
        response.put("status", charge.getStatus());
        response.put("chargeId", charge.getId());
        response.put("balance_transaction", charge.getBalanceTransaction());
        System.out.println("Charge: " + charge);
        return response;
    }

    @ExceptionHandler(StripeException.class)
    public Map<String, String> handleError(StripeException ex) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return errorResponse;
    }
}