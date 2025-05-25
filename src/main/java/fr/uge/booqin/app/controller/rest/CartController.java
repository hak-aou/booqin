package fr.uge.booqin.app.controller.rest;

import com.stripe.exception.StripeException;
import fr.uge.booqin.app.dto.cart.CartDTO;
import fr.uge.booqin.app.dto.cart.PaymentIntentDTO;
import fr.uge.booqin.app.dto.cart.CheckoutDTO;
import fr.uge.booqin.app.service.StripeService;
import fr.uge.booqin.app.service.loan.CartService;
import fr.uge.booqin.infra.external.payment.CustomChargeRequest;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/cart", "/android/cart"})
public class CartController {

    private final CartService cartService;
    private final StripeService stripeService;

    public CartController(CartService cartService, StripeService stripeService) {
        this.cartService = cartService;
        this.stripeService = stripeService;
    }

    @GetMapping("")
    public CartDTO getCart(@AuthenticationPrincipal SecurityUser currentUser) {
        return cartService.getCart(currentUser.authenticatedUser());
    }

    @DeleteMapping("/{bookId}")
    public void removeFromCart(@PathVariable("bookId") UUID bookId, @AuthenticationPrincipal SecurityUser currentUser) {
        cartService.removeBookFromCart(currentUser.authenticatedUser(), bookId);
    }

    @PostMapping("/checkout/{cartVersion}")
    public CheckoutDTO checkout(@AuthenticationPrincipal SecurityUser currentUser, @PathVariable("cartVersion") long cartVersion) {
        return cartService.checkout(currentUser.authenticatedUser(), cartVersion);
    }

    @PostMapping("/stripe/charge")
    public PaymentIntentDTO charge(@AuthenticationPrincipal SecurityUser currentUser, @RequestBody CustomChargeRequest customChargeRequest) throws StripeException {
        return stripeService.createPaymentIntent(currentUser.authenticatedUser(), customChargeRequest);
    }

    @PostMapping("/stripe/complete/{paymentIntentId}")
    public void chargeComplete(@AuthenticationPrincipal SecurityUser currentUser, @PathVariable("paymentIntentId") String paymentIntentId) throws StripeException {
        stripeService.checkPayment(paymentIntentId);
    }

}
