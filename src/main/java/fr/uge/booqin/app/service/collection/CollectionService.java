package fr.uge.booqin.app.service.collection;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.collection.CollectionCreationDTO;
import fr.uge.booqin.app.dto.collection.CollectionInfoDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.app.service.book.AuthorService;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.persistence.entity.book.AuthorEntity;
import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.collection.standard.CollectionEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.collection.CollectionRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.Validator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final BookRepository bookRepository;
    private final Validator validator;
    private final UserRepository userRepository;
    private final BookService bookService;
    private final AuthorService authorService;

    @PersistenceContext
    private EntityManager entityManager;

    public CollectionService(CollectionRepository collectionRepository,
                             BookRepository bookRepository,
                             Validator validator,
                             UserRepository userRepository,
                             BookService bookService, AuthorService authorService) {
        this.collectionRepository = collectionRepository;
        this.bookRepository = bookRepository;
        this.validator = validator;
        this.userRepository = userRepository;
        this.bookService = bookService;
        this.authorService = authorService;
    }

    @Transactional
    public CollectionEntity createUserCollection(CollectionEntity collectionEntity) {
        return collectionRepository.save(collectionEntity);
    }

    @Transactional
    public List<CollectionInfoDTO> getAllUserCollection(User user) {
        return collectionRepository.findByUserId(user.id()).stream()
                .map(this::from).toList();
    }

    public CollectionInfoDTO from(CollectionEntity collectionEntity) {
        return new CollectionInfoDTO(
                collectionEntity.getId(),
                collectionEntity.getCommentable().getId(),
                collectionEntity.getFollowable().getId(),
                collectionEntity.getTitle(),
                collectionEntity.getDescription(),
                collectionEntity.getVisibility(),
                Math.toIntExact(collectionRepository.collectionSize(collectionEntity.getId())),
                UserService.from(collectionEntity.getUser())
        );
    }

    @Transactional
    public PaginatedResult<CollectionInfoDTO> getAllPublicCollection(PageRequest request) {
        return ServiceUtils.paginatedRequest(
                validator,
                request,
                pageable ->
                        collectionRepository.findAllByVisibility(true, pageable)
                        .map(this::from)
        );
    }

    @Transactional
    public List<CollectionInfoDTO> getAllPublicCollectionOfUser(UUID userId) {
        return collectionRepository.findAllByUserIdAndVisibility(userId, true)
                .stream().map(this::from).toList();
    }

    @Transactional
    public CollectionInfoDTO findCollection(Long id, Optional<User> user) {
        return processCollectionWithMaybeUser(id, user, this::from);

    }

    @Transactional
    public void addBookToCollection(Long collectionId, UUID bookId, User user) {
        var collection = collectionRepository.findByIdOwnedByUser(collectionId, user.id())
                .orElseThrow(() -> new TheirFaultException("Collection '" + collectionId + "' not found"));
        var book = bookRepository.findById(bookId)
                .orElseThrow(() -> new TheirFaultException("Book '" + bookId + "' not found"));
        collection.addBook(book);
        collectionRepository.save(collection);
    }

    @Transactional
    public void addAllBooksToCollection(Long collectionId, List<UUID> bookIds, User user) {
        var collection = collectionRepository.findByIdOwnedByUser(collectionId, user.id())
                .orElseThrow(() -> new TheirFaultException("Collection not found"));
        var books = bookRepository.findAllById(bookIds);
        collection.addAllBooks(books);
        collectionRepository.save(collection);
    }

    @Transactional
    public void addAllBooksToCollectionSystem(Long collectionId, List<UUID> bookIds) {
        var collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new OurFaultException("Collection not found"));
        var books = bookRepository.findAllById(bookIds);
        collection.addAllBooks(books);
        collectionRepository.save(collection);
    }

    @Transactional
    public void addAllBooksEntitiesToCollectionSystem(Long collectionId, List<BookEntity> books) {
        var collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new OurFaultException("Collection not found"));
        collection.addAllBooks(books);
        collectionRepository.save(collection);
    }

    @Transactional
    public PaginatedResult<BookInfoDTO> getBooks(Long collectionId, PageRequest request, Optional<User> user) {
        return ServiceUtils.paginatedRequest(
                validator,
                request,
                pageable -> {
                    var collection = processCollectionWithMaybeUser(collectionId, user, Function.identity());
                    return bookRepository.findAllByCollections_Id(collection.getId(), pageable)
                            .map(bookService::from);
                }
        );
    }

    private <T> T processCollectionWithMaybeUser(Long collectionId, Optional<User> user, Function<CollectionEntity, T> consumer) {
        return user
                .map(User::id)
                // either the user is authenticated, and we can return any collection they own, or public collections
                .map(userId -> collectionRepository.findByIdVisibleOrOwnedByUser(collectionId, userId))
                // or the user is not authenticated, and we can only return public collections
                .orElseGet(() -> collectionRepository.findByIdWithVisibility(collectionId, true))
                .map(consumer)
                .orElseThrow(() -> new TheirFaultException("Collection not found"));
    }

    @Transactional
    public CollectionInfoDTO createCollection(CollectionCreationDTO collectionInfoDTO, User user) {
        var userEntity = userRepository.findById(user.id())
                .orElseThrow(() -> new TheirFaultException("User not found"));
        var collection = new CollectionEntity(
                collectionInfoDTO.title(),
                collectionInfoDTO.description(),
                collectionInfoDTO.visibility(),
                userEntity
        );
        return collectionToInfo(createUserCollection(collection));
    }

    @Transactional
    public CollectionInfoDTO createCollection(CollectionCreationDTO collectionInfoDTO, UserEntity userEntity) {
        var collection = new CollectionEntity(
                collectionInfoDTO.title(),
                collectionInfoDTO.description(),
                collectionInfoDTO.visibility(),
                userEntity
        );
        return collectionToInfo(createUserCollection(collection));
    }

    private CollectionInfoDTO collectionToInfo(CollectionEntity collectionEntity) {
        return new CollectionInfoDTO(
                collectionEntity.getId(),
                collectionEntity.getCommentable().getId(),
                collectionEntity.getFollowable().getId(),
                collectionEntity.getTitle(),
                collectionEntity.getDescription(),
                collectionEntity.getVisibility(),
                Math.toIntExact(collectionRepository.collectionSize(collectionEntity.getId())),
                UserService.from(collectionEntity.getUser())
        );
    }

    @Transactional
    public List<CollectionInfoDTO> findMyCollectionsContainingABook(UUID bookId, User user) {
        return collectionRepository.findByUserIdAndBook(user.id(), bookId)
                .stream().map(this::collectionToInfo).toList();
    }

    @Transactional
    public void removeBookFromCollection(Long collectionId, UUID bookId, User user) {
        var collection = collectionRepository.findByIdVisibleOrOwnedByUser(collectionId, user.id())
                .orElseThrow(() -> new TheirFaultException("Collection not found"));
        var book = bookRepository.findById(bookId);
        if (book.isEmpty()) {
            return;
        }
        collection.removeBook(book.get());
        collectionRepository.save(collection);
    }

    @Transactional
    public void removeAllBooksFromCollection(Long collectionId) {
        var collection = collectionRepository.findById(collectionId)
                .orElseThrow(() -> new OurFaultException("Collection not found"));
        collection.removeAllBooks();
        collectionRepository.save(collection);
    }

    @Transactional
    public Long findOrCreateRecentCollection(String title) {
        var collection = collectionRepository.findPublicCollectionEntitiesByTitle(title, Pageable.unpaged())
                .stream().findFirst();

        if (collection.isEmpty()) {
            var booqin = userRepository.findByUsername("booqin");

            if (booqin.isPresent()) {
                var recentCollection = createCollection(new CollectionCreationDTO(
                                title,
                                "Collection of books added recently",
                                true
                        ),
                        booqin.get());

                return recentCollection.id();
            }
        }

        return collection.get().getId();
    }

    @Transactional
    public Long findOrCreateVotedBooksCollection(User user) {
        var title = "My voted books";
        var collection = collectionRepository.findByOwnerIdAndTitle(user.id(), title)
                .stream().findFirst();

        if (collection.isEmpty()) {
            var myVotedCollection = createCollection(new CollectionCreationDTO(
                            title,
                            "All my voted books collection",
                            false
                    ),
                    user);

            return myVotedCollection.id();
        }

        return collection.get().getId();
    }

    @Transactional
    public Long findOrCreateAuthorCollection(String author) {
        var title = author + ""+ (author.endsWith("s") ? "'" : "s") + " collection";

        var authorCollection = collectionRepository.findPublicCollectionEntitiesByTitle(title, Pageable.unpaged())
                .stream().findFirst();

        if (authorCollection.isEmpty()) {
            var booqin = userRepository.findByUsername("booqin");

            if (booqin.isPresent()) {
                var recentCollection = createCollection(new CollectionCreationDTO(
                                title,
                                "All books of " + author,
                                true
                        ),
                        booqin.get());

                return recentCollection.id();
            }
        }

        return authorCollection.get().getId();
    }

    @Transactional
    public int collectionRecentBooks(int days) {
        var recentBooks = bookService.getRecentBooks(days);

        // get
        var recentCollectionId = findOrCreateRecentCollection("Books added last " + days + " days");

        // remove
        removeAllBooksFromCollection(recentCollectionId);

        // add
        addAllBooksEntitiesToCollectionSystem(recentCollectionId, recentBooks);

        return recentBooks.size();
    }

    @Transactional
    public void createOneCollectionPerAuthor() {
        // get authors
        var authors = authorService.findAllAuthors().stream().map(AuthorEntity::getName).toList();

        for (var author : authors) {
            // get collection per author
            var authorCollection = findOrCreateAuthorCollection(author);

            // get
            var books = bookService.findBooksByAuthor(author);

            // remove
            removeAllBooksFromCollection(authorCollection);

            // add
            addAllBooksEntitiesToCollectionSystem(authorCollection, books);
        }
    }

    @Transactional
    public void updateCollection(Long collectionId, CollectionCreationDTO collectionInfoDTO, User user) {
        var collection = collectionRepository.findByIdOwnedByUser(collectionId, user.id())
                .orElseThrow(() -> new TheirFaultException("Collection not found"));
        collection.setTitle(collectionInfoDTO.title());
        collection.setDescription(collectionInfoDTO.description());
        collection.setVisibility(collectionInfoDTO.visibility());
        collectionRepository.save(collection);
    }

    @Transactional
    public void deleteCollection(Long collectionId, User user) {
        var collection = Optional.ofNullable(
                entityManager.find(CollectionEntity.class, collectionId, LockModeType.PESSIMISTIC_WRITE)
        ).orElseThrow(() -> new TheirFaultException("Collection not found"));
        if(!collection.getUser().getId().equals(user.id())
                && !user.isAdmin()) {
            return;
        }
        collectionRepository.delete(collection);
    }
}
