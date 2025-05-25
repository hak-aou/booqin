package fr.uge.booqin.app.service.collection;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.collection.smart.SmartCollectionCreationDTO;
import fr.uge.booqin.app.dto.collection.smart.SmartCollectionInfoDTO;
import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.dto.filter.IntervalDateDTO;
import fr.uge.booqin.app.dto.filter.IntervalNumberDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.collection.smart.AuthorFilterService;
import fr.uge.booqin.app.service.collection.smart.CategoryFilterService;
import fr.uge.booqin.app.service.collection.smart.LanguageFilterService;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.AuthorFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.CategoryFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.DateFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.FilterLogic;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.HasSubtitleFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.LanguageFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.PageCountFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.TitleFilterEntity;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.collection.CollectionRepository;
import fr.uge.booqin.infra.persistence.repository.collection.SmartCollectionRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.validation.Validator;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SmartCollectionService {

    private final CollectionRepository collectionRepository;
    private final SmartCollectionRepository smartCollectionRepository;
    private final BookService bookService;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private final CategoryFilterService categoryFilterService;
    private final LanguageFilterService languageFilterService;
    private final AuthorFilterService authorFilterService;

    private final Validator validator;

    public SmartCollectionService(
            CollectionRepository collectionRepository,
            SmartCollectionRepository smartCollectionRepository,
            BookService bookService, BookRepository bookRepository,
            UserRepository userRepository,
            Validator validator,
            CategoryFilterService categoryFilterService,
            LanguageFilterService languageFilterService,
            AuthorFilterService authorFilterService
    ) {
        this.collectionRepository = collectionRepository;
        this.smartCollectionRepository = smartCollectionRepository;
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.validator = validator;
        this.categoryFilterService = categoryFilterService;
        this.languageFilterService = languageFilterService;
        this.authorFilterService = authorFilterService;
    }

    @Transactional
    public PaginatedResult<BookInfoDTO> filterBooks(Long collectionId,
                                                    FilterBooksDTO filterBooksDTO,
                                                    PageRequest pageRequest) {
        return ServiceUtils.paginatedRequest(
                validator,
                pageRequest,
                pageable -> bookService.findBooksInCollectionWithFilters(collectionId, filterBooksDTO, pageable)
                        .map(bookService::from)
        );
    }

    @Transactional
    public SmartCollectionEntity createUserSmartCollection(SmartCollectionEntity smartCollectionEntity) {
        return smartCollectionRepository.save(smartCollectionEntity);
    }


    private SmartCollectionInfoDTO collectionToInfo(SmartCollectionEntity smartCollectionEntity) {
        return new SmartCollectionInfoDTO(
                smartCollectionEntity.getId(),
                smartCollectionEntity.getCommentable().getId(),
                smartCollectionEntity.getFollowable().getId(),
                smartCollectionEntity.getTitle(),
                smartCollectionEntity.getDescription(),
                smartCollectionEntity.getVisibility(),
                Math.toIntExact(collectionRepository.collectionSize(smartCollectionEntity.getId())),
                UserService.from(smartCollectionEntity.getUser()),
                smartCollectionEntity.getFilterBooksDTO()
        );
    }


    public Set<SmartCollectionFilterEntity> from(SmartCollectionEntity smartCollectionEntity,
                                                 FilterBooksDTO filterBooksDTO) {
        HashSet<SmartCollectionFilterEntity> filters = new HashSet<>();

        // Category Filter
        filters.addAll(categoryFilterService.createFilters(smartCollectionEntity, filterBooksDTO));

        // Language Filter
        filters.addAll(languageFilterService.createFilters(smartCollectionEntity, filterBooksDTO));

        // Author Filter
        filters.addAll(authorFilterService.createFilters(smartCollectionEntity, filterBooksDTO));

        // Title Filter
        if (filterBooksDTO.title() != null) {
            TitleFilterEntity titleFilter = new TitleFilterEntity(filterBooksDTO.title());
            titleFilter.setSmartCollection(smartCollectionEntity);
            filters.add(titleFilter);
        }

        // HasSubtitle Filter
        if (filterBooksDTO.hasSubtitle() != null) {
            HasSubtitleFilterEntity subtitleFilter = new HasSubtitleFilterEntity(filterBooksDTO.hasSubtitle());
            subtitleFilter.setSmartCollection(smartCollectionEntity);
            filters.add(subtitleFilter);
        }

        // Page Count Filter
        if (filterBooksDTO.pageCountInterval() != null) {
            if (filterBooksDTO.pageCountInterval().min() > 0) {
                PageCountFilterEntity minPageFilter = new PageCountFilterEntity(filterBooksDTO.pageCountInterval().min(), FilterLogic.AFTER);
                minPageFilter.setSmartCollection(smartCollectionEntity);
                filters.add(minPageFilter);
            }
            if (filterBooksDTO.pageCountInterval().max() > 0) {
                PageCountFilterEntity maxPageFilter = new PageCountFilterEntity(filterBooksDTO.pageCountInterval().max(), FilterLogic.BEFORE);
                maxPageFilter.setSmartCollection(smartCollectionEntity);
                filters.add(maxPageFilter);
            }
        }

        // Published Date Filter
        if (filterBooksDTO.publishedDateInterval() != null) {
            if (filterBooksDTO.publishedDateInterval().min() != null) {
                DateFilterEntity minDateFilter = new DateFilterEntity(filterBooksDTO.publishedDateInterval().min(), FilterLogic.AFTER);
                minDateFilter.setSmartCollection(smartCollectionEntity);
                filters.add(minDateFilter);
            }
            if (filterBooksDTO.publishedDateInterval().max() != null) {
                DateFilterEntity maxDateFilter = new DateFilterEntity(filterBooksDTO.publishedDateInterval().max(), FilterLogic.BEFORE);
                maxDateFilter.setSmartCollection(smartCollectionEntity);
                filters.add(maxDateFilter);
            }
        }

        return filters;
    }

    @Transactional
    public void addBooksToSmartCollection(SmartCollectionEntity smartCollection) {
        var books = bookService.findAllBooksWithFilters(smartCollection.getFilterBooksDTO(), Pageable.unpaged());

        for (var book : books) {
            smartCollection.addBook(book);
        }
    }

    @Transactional
    public SmartCollectionInfoDTO createSmartCollection(SmartCollectionCreationDTO smartCollectionCreationDTO,
                                                        User user) {
        var userEntity = userRepository.findById(user.id())
                .orElseThrow(() -> new TheirFaultException("User not found"));

        var smartCollection = new SmartCollectionEntity(
                smartCollectionCreationDTO.title(),
                smartCollectionCreationDTO.description(),
                smartCollectionCreationDTO.visibility(),
                userEntity,
                new HashSet<>()
        );

        // set filter to smart collection
        var filters = from(smartCollection, smartCollectionCreationDTO.filterBooksDTO());
        filters.forEach(filter -> filter.setSmartCollection(smartCollection));

        smartCollection.setFilters(filters);
        smartCollection.setFilterBooksDTO(smartCollectionCreationDTO.filterBooksDTO());

        // add books to smart collections
        addBooksToSmartCollection(smartCollection);

        return collectionToInfo(createUserSmartCollection(smartCollection));
    }

    @Transactional
    public SmartCollectionInfoDTO getSmartCollection(Long collectionId) {
        return smartCollectionRepository.findById(collectionId)
                .map(this::collectionToInfo)
                .orElseThrow(() -> new TheirFaultException("Collection not found"));
    }


    /* Get filter from collection */
    public static FilterBooksDTO convertTo(Set<SmartCollectionFilterEntity> filters) {
        String title = null;
        Boolean hasSubtitle = null;
        List<String> categories = new ArrayList<>();
        List<String> languages = new ArrayList<>();
        List<String> authors = new ArrayList<>();
        IntervalNumberDTO pageCountInterval = new IntervalNumberDTO(0, 0);
        IntervalDateDTO publishedDateInterval = new IntervalDateDTO(null, null);

        for (SmartCollectionFilterEntity filter : filters) {
            switch (filter) {
                case CategoryFilterEntity categoryFilter ->
                        categories.add(categoryFilter.getCategory().getCategoryName());
                case LanguageFilterEntity languageFilter ->
                        languages.add(languageFilter.getLanguage().getLanguageName());
                case AuthorFilterEntity authorFilter -> authors.add(authorFilter.getAuthor().getName());
                case TitleFilterEntity titleFilter -> title = titleFilter.getTitle();
                case HasSubtitleFilterEntity subtitleFilter -> hasSubtitle = subtitleFilter.getHasSubtitle();
                case PageCountFilterEntity pageCountFilter -> {
                    if (pageCountFilter.getLogic() == FilterLogic.AFTER) {
                        pageCountInterval = new IntervalNumberDTO(pageCountFilter.getNumber(), pageCountInterval.max());
                    } else {
                        pageCountInterval = new IntervalNumberDTO(pageCountInterval.min(), pageCountFilter.getNumber());
                    }
                }
                case DateFilterEntity dateFilter -> {
                    if (dateFilter.getLogic() == FilterLogic.AFTER) {
                        publishedDateInterval = new IntervalDateDTO(dateFilter.getDate(), publishedDateInterval.max());
                    } else {
                        publishedDateInterval = new IntervalDateDTO(publishedDateInterval.min(), dateFilter.getDate());
                    }
                }
                default -> throw new TheirFaultException("Unknown filter type: " + filter.getClass().getSimpleName());
            }
        }

        return new FilterBooksDTO(
                title,
                hasSubtitle,
                categories,
                languages,
                authors,
                pageCountInterval,
                publishedDateInterval
        );
    }

    @Transactional
    public FilterBooksDTO getSmartCollectionFilter(Long collectionId) {
        var filters = smartCollectionRepository.findFiltersByCollectionId(collectionId);
        return convertTo(filters);
    }

    @Transactional
    public List<SmartCollectionEntity> findAllSmartCollections() {
        return smartCollectionRepository.findAll();
    }


    @Transactional
    public int updateAllSmartCollection(int days) {
        // get
        var recentBooks = bookService.getRecentBooks(days);
        var smartCollections = findAllSmartCollections();

        if(recentBooks.isEmpty() || smartCollections.isEmpty()) {
            return 0;
        }

        // update smart collection with new recent books
        for (var smartCollection : smartCollections) {
            var filterDTO = SmartCollectionService.convertTo(smartCollection.getFilters());
            var booksToAdd = bookService.findFilteredBooksFromListBooks(
                    filterDTO,
                    Pageable.unpaged(),
                    recentBooks
            );

            smartCollection.addAllBooks(booksToAdd.stream().toList());
        }

        return recentBooks.size();
    }
}
