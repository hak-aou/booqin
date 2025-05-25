package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.collection.smart.SmartCollectionCreationDTO;
import fr.uge.booqin.app.dto.collection.smart.SmartCollectionInfoDTO;
import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.dto.filter.FilterBooksRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.service.collection.SmartCollectionService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/smart_collection", "/android/smart_collection"})
public class SmartCollectionRestController {

    private final SmartCollectionService smartCollectionService;

    public SmartCollectionRestController(SmartCollectionService smartCollectionService) {
        this.smartCollectionService = smartCollectionService;
    }

    @PostMapping("/filter/{collectionId}/books")
    public PaginatedResult<BookInfoDTO> getBookOfCollection(@PathVariable Long collectionId,
                                                            @RequestBody FilterBooksRequest filterBooksRequest) {
        var filterBooksDTO = filterBooksRequest.filterBooksDTO();
        var pageRequest = filterBooksRequest.pageRequest();

        return smartCollectionService.filterBooks(collectionId, filterBooksDTO, pageRequest);
    }

    @PostMapping("")
    public SmartCollectionInfoDTO createSmartCollection(@RequestBody SmartCollectionCreationDTO smartCollectionCreationDTO,
                                                        @AuthenticationPrincipal SecurityUser currentUser) {

        return smartCollectionService.createSmartCollection(smartCollectionCreationDTO, currentUser.authenticatedUser());
    }

    @GetMapping("{collectionId}")
    public SmartCollectionInfoDTO getSmartCollection(@PathVariable Long collectionId) {
        return smartCollectionService.getSmartCollection(collectionId);
    }

    @GetMapping("/filter/{collectionId}")
    public FilterBooksDTO getSmartCollectionFilter(@PathVariable Long collectionId) {
        return smartCollectionService.getSmartCollectionFilter(collectionId);
    }
}
