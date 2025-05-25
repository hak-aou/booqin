package fr.uge.booqin.app.controller.mvc;

import fr.uge.booqin.infra.external.payment.enums.ChargeRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/stripe")
public class StripeController {

    @Value("${stripe.api.key}")
    private String stripePublicKey;

    @GetMapping("/checkout")
    public String checkoutWithStripe(Model model) {
        model.addAttribute("amount", 50 * 100); // In cents
        model.addAttribute("stripePublicKey", stripePublicKey);
        model.addAttribute("currency", ChargeRequest.Currency.EUR.name());
        return "payment";
    }

}