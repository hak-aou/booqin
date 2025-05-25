package fr.uge.booqin.app.service.smartcollection;

import fr.uge.booqin.app.service.book.BookService;
import fr.uge.booqin.app.service.book.ScheduledTasks;
import fr.uge.booqin.app.service.collection.SmartCollectionService;
import fr.uge.booqin.infra.persistence.entity.collection.smart.SmartCollectionEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.TitleFilterEntity;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import fr.uge.booqin.infra.persistence.fixtures.BookFixtures;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(
        locations = "/application-test.properties",
        properties = {
                "spring.profiles.active=test"
        }
)
@Transactional
public class ScheduledTasksTest {
    @Autowired
    private BookFixtures bookFixtures;

    @Autowired
    private ScheduledTasks scheduledTasks;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private SmartCollectionService smartCollectionService;

    @Test
    @Rollback
    void testSmartCollectionUpdateScheduling() {

        // create books
        var books1 = bookFixtures.randomBooks(20);
        var books2 = bookFixtures.randomBooks(20);

        var book1 = bookFixtures.randomBook();
        book1.setTitle("The book 1");

        var book2 = bookFixtures.randomBook();
        book2.setTitle("New book 2");

        var book3 = bookFixtures.randomBook();
        book3.setTitle("The book 3");

        var recentBooks = List.of(
                book1,
                book2,
                book3
        );

        books1.forEach(b -> b.setAddedAt(Instant.now().minus(30, ChronoUnit.DAYS)));
        books2.forEach(b -> b.setAddedAt(Instant.now().minus(30, ChronoUnit.DAYS)));

        bookRepository.saveAll(books1);
        bookRepository.saveAll(books2);
        bookRepository.saveAll(recentBooks);


        // create filters
        Set<SmartCollectionFilterEntity> theTitleFilter = Set.of(
                new TitleFilterEntity("The")
        );
        Set<SmartCollectionFilterEntity> newTitleFilter = Set.of(
                new TitleFilterEntity("New")
        );

        // create smart collection
        var smartCollectionTitle = new SmartCollectionEntity();
        smartCollectionTitle.setBooks(books1);
        smartCollectionTitle.setFilters(theTitleFilter);

        var smartCollectionCategory = new SmartCollectionEntity();
        smartCollectionCategory.setBooks(books2);
        smartCollectionCategory.setFilters(newTitleFilter);

        smartCollectionService.createUserSmartCollection(smartCollectionTitle);
        smartCollectionService.createUserSmartCollection(smartCollectionCategory);

        // test before update
        assertEquals(20, smartCollectionTitle.getBooks().size());
        assertEquals(20, smartCollectionCategory.getBooks().size());

        var mustAddBooks = scheduledTasks.updateAllSmartCollection();
        assertEquals(3, mustAddBooks);

        // test after update
        assertEquals(22, smartCollectionTitle.getBooks().size());
        assertEquals(21, smartCollectionCategory.getBooks().size());


    }

}
