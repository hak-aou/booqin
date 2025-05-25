package fr.uge.booqin.app.controller.mvc;

import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.service.collection.CollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Just a demo mvc controller
 */
@Controller
public class HomeController {

    private final CollectionService collectionService;

    public HomeController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("collections", collectionService.getAllPublicCollection(new PageRequest(0, 10)));
        return "home";
    }
}
