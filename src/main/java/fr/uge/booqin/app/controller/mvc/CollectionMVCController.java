package fr.uge.booqin.app.controller.mvc;

import fr.uge.booqin.app.controller.mvc.session.SessionData;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.service.collection.CollectionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/collections")
public class CollectionMVCController {

    private final CollectionService collectionService;
    private final SessionData session;

    public CollectionMVCController(CollectionService collectionService, SessionData session) {
        this.collectionService = collectionService;
        this.session = session;
    }

    // localhost:8080/collections/1
    @GetMapping("/{collectionId}")
    public String getCollection(
            @RequestParam Optional<Integer> offset,
            @RequestParam Optional<Integer> limit,
            @PathVariable("collectionId") Long collectionId,
            Model model
    ) {
        model.addAttribute("collection",
                collectionService.findCollection(collectionId, Optional.ofNullable(session.user()))
        );
        model.addAttribute(
                "page",
                collectionService.getBooks(collectionId,
                        new PageRequest(offset.orElse(0), limit.orElse(10)),
                        Optional.ofNullable(session.user()))
        );
        return "collection";
    }
}
