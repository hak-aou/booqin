package fr.uge.booqin.app.controller.mvc;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Fallback controller for SPA.
 * This controller is used to redirect all requests intended for the SPA to the index.html file.
 */
@Controller
public class SPAFallbackController {

    @RequestMapping(value = "/spa")
    public String forwardSPAIndex() {
        return "forward:/spa/index.html";
    }

    @RequestMapping(value = "/spa/{path:[^.]*}")
    public String forwardSPA() {
        return "forward:/spa/index.html";
    }
}