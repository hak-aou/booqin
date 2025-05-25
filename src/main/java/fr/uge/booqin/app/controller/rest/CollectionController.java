package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.collection.CollectionCreationDTO;
import fr.uge.booqin.app.dto.collection.CollectionInfoDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.service.collection.CollectionService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping({"/api/collection", "/android/collection"})
public class CollectionController {

    private final CollectionService collectionService;

    public CollectionController(CollectionService collectionService) {
        this.collectionService = collectionService;
    }

    @GetMapping("/getAll")
    public List<CollectionInfoDTO> getAllUserCollection(@AuthenticationPrincipal SecurityUser currentUser) {
        return collectionService.getAllUserCollection(currentUser.authenticatedUser());
    }

    @GetMapping("/{collectionId}")
    public CollectionInfoDTO getUserCollection(@PathVariable Long collectionId,
                                               @AuthenticationPrincipal SecurityUser currentUser) {
        return collectionService.findCollection(
                collectionId,
                Optional.ofNullable(currentUser)
                        .map(cu -> currentUser.authenticatedUser())
        );
    }

    @PostMapping("/getAllPublic")
    public PaginatedResult<CollectionInfoDTO> getAllPublicCollection(@RequestBody PageRequest request) {
        return collectionService.getAllPublicCollection(request);
    }

    /*
        @Todo:
            Create a new BookRequest(PageRequest, filteringRequest)
            to improve search functionality
     */
    @PostMapping("/{collectionId}/books")
    public PaginatedResult<BookInfoDTO> getBookOfCollection(@PathVariable Long collectionId,
                                                           @RequestBody PageRequest request,
                                                           @AuthenticationPrincipal SecurityUser currentUser) {
        return collectionService.getBooks(collectionId, request,
                Optional.ofNullable(currentUser)
                .map(cu -> currentUser.authenticatedUser()));
    }

    @PostMapping("")
    public CollectionInfoDTO createCollection(@RequestBody CollectionCreationDTO collectionInfoDTO,
                                              @AuthenticationPrincipal SecurityUser currentUser) {
        return collectionService.createCollection(collectionInfoDTO, currentUser.authenticatedUser());
    }

    @GetMapping("/contains/{bookId}")
    public List<CollectionInfoDTO> whichOfMyCollectionContainsABook(@PathVariable UUID bookId,
                                                  @AuthenticationPrincipal SecurityUser currentUser) {
        return collectionService.findMyCollectionsContainingABook(bookId, currentUser.authenticatedUser());
    }

    @PostMapping("/{collectionId}/book/{bookId}")
    public void addBookToCollection(@PathVariable Long collectionId,
                                    @PathVariable UUID bookId,
                                    @AuthenticationPrincipal SecurityUser currentUser) {
        collectionService.addBookToCollection(collectionId, bookId, currentUser.authenticatedUser());
    }

    @DeleteMapping("/{collectionId}/book/{bookId}")
    public void removeBookFromCollection(@PathVariable Long collectionId,
                                         @PathVariable UUID bookId,
                                         @AuthenticationPrincipal SecurityUser currentUser) {
        collectionService.removeBookFromCollection(collectionId, bookId, currentUser.authenticatedUser());
    }

    @PatchMapping("/{collectionId}")
    public void updateCollection(@PathVariable Long collectionId,
                                 @RequestBody CollectionCreationDTO collectionInfoDTO,
                                 @AuthenticationPrincipal SecurityUser currentUser) {
        collectionService.updateCollection(collectionId, collectionInfoDTO, currentUser.authenticatedUser());
    }

    @DeleteMapping("/{collectionId}")
    public void deleteCollection(@PathVariable Long collectionId,
                                 @AuthenticationPrincipal SecurityUser currentUser) {
        collectionService.deleteCollection(collectionId, currentUser.authenticatedUser());
    }

}
