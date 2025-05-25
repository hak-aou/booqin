package fr.uge.booqin.app.service.book;

import fr.uge.booqin.app.service.collection.CollectionService;
import fr.uge.booqin.app.service.collection.SmartCollectionService;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.collection.CollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

// https://www.baeldung.com/running-setup-logic-on-startup-in-spring
// https://spring.io/guides/gs/scheduling-tasks

@Component
public class ScheduledTasks {

    private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final BookService bookService;
    private final CollectionRepository collectionRepository;
    private final CollectionService collectionService;
    private final SmartCollectionService smartCollectionService;
    private final BooqInConfig config;
    private final BookRepository bookRepository;

    public ScheduledTasks(
            BooqInConfig config,
            BookService bookService,
            CollectionRepository collectionRepository,
            CollectionService collectionService,
            BookRepository bookRepository,
            SmartCollectionService smartCollectionService) {
        this.config = config;
        this.bookService = bookService;
        this.bookRepository = bookRepository;
        this.collectionRepository = collectionRepository;
        this.collectionService = collectionService;
        this.smartCollectionService = smartCollectionService;
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public void updateDatabaseFromApi() throws IOException {
        log.info("Fetching book from API starting at {}", dateFormat.format(new Date()));
        log.info("In progress...");

        // open a file, from path
        var books = bookService.getBooksFromAuthorsFile(config.authorsFile(), config.numberAuthorToFetch());

        var bookEntities = bookService.insertBooks(books);
        // very bad @Todo: use observer
        var globalCollection = collectionRepository.findPublicCollectionEntitiesByTitle("Books", Pageable.unpaged())
                .stream().findFirst().orElseThrow();
        collectionService.addAllBooksEntitiesToCollectionSystem(globalCollection.getId(), bookEntities);

        // @Todo add to stock manager

        log.info("Finish at {}, {} new books added\n\n", dateFormat.format(new Date()), books.size());
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void createRecentBooksCollection() {
        log.info("Creating recent books collection starting at {}", dateFormat.format(new Date()));
        log.info("In progress...");

        var aa = bookRepository.count();
        var nbBooks = collectionService.collectionRecentBooks(config.fetchRecentBooksDays());

        log.info("Finish, {} new books added in the collection 'Books added last {} days'\n\n", nbBooks, config.fetchRecentBooksDays());
    }

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public int updateAllSmartCollection() {
        log.info("Updating all smart collections starting at {}", dateFormat.format(new Date()));
        log.info("In progress...");

        var nbBooks = smartCollectionService.updateAllSmartCollection(config.updateSmartCollectionDayToFetch());

        log.info("Finish, all smart collections updated with {} new books\n\n", nbBooks);

        return nbBooks;
    }

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void creationOneCollectionPerAuthor() {
        log.info("Creating recent books collection starting at {}", dateFormat.format(new Date()));
        log.info("In progress...");

        collectionService.createOneCollectionPerAuthor();

        log.info("Finish, creation of one collection per author\n\n");
    }
}