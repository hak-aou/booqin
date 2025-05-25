package fr.uge.booqin.app.controller.mvc;

import com.stripe.exception.StripeException;
import com.stripe.model.Charge;
import fr.uge.booqin.app.service.StripeService;
import fr.uge.booqin.infra.external.payment.enums.ChargeRequest;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/charge")
public class ChargeController {

    private final StripeService stripeService;

    public ChargeController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @Before("execution(* fr.uge.booqin.app.controller.mvc.ChargeController.display(..))")
    @PostMapping
    public String charge(@ModelAttribute ChargeRequest chargeRequest, Model model) throws StripeException {
        System.out.println("debut ChargeController");
        System.out.println("Charge request: " + chargeRequest);
        Charge charge = stripeService.charge(chargeRequest);
        model.addAttribute("id", charge.getId());
        model.addAttribute("status", charge.getStatus());
        model.addAttribute("chargeId", charge.getId());
        model.addAttribute("balance_transaction", charge.getBalanceTransaction());
        return "payment-result";
    }

    @ExceptionHandler(StripeException.class)
    public String handleError(Model model, StripeException ex) {
        model.addAttribute("error", ex.getMessage());
        return "payment-result";
    }
    public void display() {
        System.out.println("display");
    }
}
