package fr.uge.booqin.app.service.search;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.collection.CollectionInfoDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.app.service.collection.CollectionService;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.collection.CollectionRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;

@Service
public class SearchService {


    private final BookRepository bookRepository;
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final Validator validator;
    private final BookService bookService;
    private final CollectionService collectionService;

    public SearchService(
            CollectionService collectionService,
            BookRepository bookRepository,
            CollectionRepository collectionRepository,
            UserRepository userRepository,
            Validator validator,
            BookService bookService
    ) {
        this.collectionRepository = collectionRepository;
        this.bookRepository = bookRepository;
        this.collectionService = collectionService;
        this.userRepository = userRepository;
        this.validator = validator;
        this.bookService = bookService;
    }

    @Transactional
    public PaginatedResult<BookInfoDTO> searchBooks(String query, PageRequest pageRequest) {
        String lowerCaseQuery = query.toLowerCase();
        return ServiceUtils.paginatedRequest(
                validator,
                pageRequest,
                pageable -> bookRepository.findBookEntitiesByTitle(lowerCaseQuery, pageable)
                        .map(bookService::from)
        );
    }

    @Transactional
    public PaginatedResult<CollectionInfoDTO> searchCollections(String query, PageRequest pageRequest) {
        String lowerCaseQuery = query.toLowerCase();
        return ServiceUtils.paginatedRequest(
                validator,
                pageRequest,
                pageable -> collectionRepository.findPublicCollectionEntitiesByTitle(lowerCaseQuery, pageable)
                        .map(collectionService::from)
        );
    }

    @Transactional
    public PaginatedResult<PublicProfileDTO> searchUsers(String query, PageRequest pageRequest) {
        String lowerCaseQuery = query.toLowerCase();
        return ServiceUtils.paginatedRequest(
                validator,
                pageRequest,
                pageable -> userRepository.findUserEntitiesByUsername(lowerCaseQuery, pageable)
                        .map(UserService::from)
        );
    }


}