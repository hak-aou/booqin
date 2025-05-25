package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.collection.CollectionInfoDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.app.service.search.SearchService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/search", "/android/search"})
public class SearchRestController {

    private final
    SearchService searchService;

    public SearchRestController(SearchService searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/books/{searchData}")
    public PaginatedResult<BookInfoDTO> searchBooks(@PathVariable("searchData") String query,
                                                   @RequestBody PageRequest request) {
        return searchService.searchBooks(query, request);
    }

    @PostMapping("/collections/{searchData}")
    public PaginatedResult<CollectionInfoDTO> searchCollections(@PathVariable("searchData") String query,
                                                                @RequestBody PageRequest request) {
        return searchService.searchCollections(query, request);
    }

    @PostMapping("/users/{searchData}")
    public PaginatedResult<PublicProfileDTO> searchUsers(@PathVariable("searchData") String query,
                                                                @RequestBody PageRequest request) {
        return searchService.searchUsers(query, request);
    }

}