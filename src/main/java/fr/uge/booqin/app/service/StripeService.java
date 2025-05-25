package fr.uge.booqin.app.service;

import com.stripe.Stripe;
import com.stripe.exception.*;
import com.stripe.model.Charge;
import com.stripe.model.PaymentIntent;
import fr.uge.booqin.app.dto.cart.PaymentIntentDTO;
import fr.uge.booqin.app.service.loan.CartService;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.external.payment.CustomChargeRequest;
import fr.uge.booqin.infra.external.payment.enums.ChargeRequest;
import fr.uge.booqin.infra.security.auth.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.endpoint.InvalidEndpointRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;

@Service
public class StripeService {
    private final Logger logger = Logger.getLogger(StripeService.class.getName());

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public StripeService(CartService cartService, JwtUtil jwtUtil,
                         @Value("${stripe.api.key}") String secretKey) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
        Stripe.apiKey = secretKey;
    }

    public Charge charge(ChargeRequest chargeRequest)
            throws StripeException, InvalidEndpointRequestException {
        Map<String, Object> chargeParams = new HashMap<>();
        chargeParams.put("amount", chargeRequest.getAmount());
        chargeParams.put("currency", chargeRequest.getCurrency());
        chargeParams.put("description", chargeRequest.getDescription());
        chargeParams.put("source", chargeRequest.getStripeToken());
        return Charge.create(chargeParams);
    }

    ///
    /// Get the amount from the order and create a payment intent
    ///
    @Transactional
    public PaymentIntentDTO createPaymentIntent(User user, CustomChargeRequest chargeRequest) throws StripeException {
        var maybeOrderAmount = cartService.getOrderAmount(user, chargeRequest.getOrderId(), chargeRequest.getCartVersion());
        if (maybeOrderAmount.isEmpty()) {
            return PaymentIntentDTO.errored("Couldn't process the payment. " +
                    "The order is either invalid, already paid or doesn't exist.");
        }
        var params = new HashMap<String, Object>();
        params.put("amount", (int) (maybeOrderAmount.orElseThrow() * 100));
        params.put("currency", "usd");
        params.put("payment_method", chargeRequest.getPaymentMethodId());
        params.put("automatic_payment_methods", Map.of(
                "enabled", true,
                "allow_redirects", "always"
        ));
        params.put("metadata", Map.of(
                // metadata stored as JWT, to avoid tampering
                "orderId", jwtUtil.generateToken(chargeRequest.getOrderId().toString(), Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))),
                "userId", jwtUtil.generateToken(user.id().toString(), Date.from(Instant.now().plus(1, ChronoUnit.MINUTES))),
                "cartVersion", jwtUtil.generateToken(chargeRequest.getCartVersion().toString(), Date.from(Instant.now().plus(1, ChronoUnit.MINUTES)))
        ));
        PaymentIntent intent = PaymentIntent.create(params);
        return PaymentIntentDTO.fromIntent(intent, false, null);
    }

    ///
    /// Check the payment intent status and complete the order if it's successful
    ///
    @Transactional
    public PaymentIntent checkPayment(String paymentIntentId) throws StripeException {
        var paymentCheck = PaymentIntent.retrieve(paymentIntentId);
        if(paymentCheck.getStatus().equals("succeeded")){
            var metadata = paymentCheck.getMetadata();
            UUID orderId;
            UUID userId;
            long cartVersion;
            try {
                // check metadata integrity
                orderId = UUID.fromString(jwtUtil.validateAndGetSubject(metadata.get("orderId")));
                userId = UUID.fromString(jwtUtil.validateAndGetSubject(metadata.get("userId")));
                cartVersion = Long.parseLong(jwtUtil.validateAndGetSubject(metadata.get("cartVersion")));
            } catch (Exception e) {
                throw new TheirFaultException("Invalid metadata in payment intent");
            }
            cartService.completeOrder(userId, orderId, cartVersion,"stripe", paymentCheck.getId());
        }
        return paymentCheck;
    }
}