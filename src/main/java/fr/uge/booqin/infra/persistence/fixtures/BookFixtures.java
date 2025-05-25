package fr.uge.booqin.infra.persistence.fixtures;

import com.github.javafaker.Faker;
import fr.uge.booqin.app.service.book.*;
import fr.uge.booqin.domain.model.books.ImageFormatModel;
import fr.uge.booqin.infra.persistence.entity.book.*;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

@Component
public class BookFixtures {
    private static final Logger logger = LoggerFactory.getLogger(BookFixtures.class);
    private final String activeProfile;
    private final Boolean fixturesEnabled;
    private final Faker faker = new Faker();

    private final BookRepository bookRepository;

    private final BookService bookService;

    private final List<AuthorEntity> authors = new ArrayList<>();
    private final List<PublisherEntity> publishers = new ArrayList<>();
    private final List<CategoryEntity> categories = new ArrayList<>();
    private final CategoryService categoryService;
    private final LanguageService languageService;
    private final AuthorService authorService;
    private final PublisherService publisherService;

    public BookFixtures(
            BookRepository bookRepository,
            BookService bookService,
            CategoryService categoryService,
            LanguageService languageService,
            AuthorService authorService,
            PublisherService publisherService,
            @Value("${spring.profiles.active:}") String activeProfile,
            @Value("${booqin.fixtures.enabled:}") Boolean fixturesEnabled
    ) {
        this.bookRepository = bookRepository;
        this.bookService = bookService;
        this.activeProfile = activeProfile;
        this.categoryService = categoryService;
        this.languageService = languageService;
        this.fixturesEnabled = fixturesEnabled;
        this.authorService = authorService;
        this.publisherService = publisherService;
    }

    @PostConstruct
    @Transactional
    public void init() {
        if (!fixturesEnabled) {
            return;
        }
        createDataset();
    }

    @Transactional
    public void createDataset() {
        logger.info("Initializing BookFixtures");
        authors.addAll(randomAuthors(10));
        publishers.addAll(randomPublishers(5));
        categories.addAll(randomCategories(10));
    }

    @Transactional
    public BookEntity createBook(String isbn13,
                                  String isbn10,
                                  String title,
                                  String subtitle,
                                  Set<AuthorEntity> authors,
                                  Set<PublisherEntity> publishers,
                                  Set<CategoryEntity> categories,
                                  LanguageEntity language,
                                  LocalDate publishedDate,
                                  String description) {
        var random = new Random();
        var book = new BookEntity();
        book.setIsbn13(isbn13);
        book.setIsbn10(isbn10);
        book.setTitle(title);
        book.setSubtitle(subtitle);
        book.setAuthors(authors);
        book.setPublishers(publishers);
        book.setPublishedDate(publishedDate);
        book.setDescription(description);
        book.setLanguage(language);
        book.setCategories(categories);
        book.setImageLinks(new ImageFormatModel(
                "https://covers.openlibrary.org/b/id/6639927-S.jpg",
                "https://covers.openlibrary.org/b/id/6639927-M.jpg",
                "https://covers.openlibrary.org/b/id/6639927-L.jpg"));
        book.setAddedAt(Instant.now());
        book.setPageCount(random.nextInt(800));
        return bookRepository.save(book);
    }

    @Transactional
    public AuthorEntity createAuthor(String lastName, String firstName) {
        return createAuthorImpl(lastName, firstName);
    }

    private AuthorEntity createAuthorImpl(String lastName, String firstName) {
        return authorService.findOrCreateAuthor(firstName + " " + lastName);
    }

    @Transactional
    public PublisherEntity createPublisher(String name) {
        return publisherService.findOrCreatePublisher(name);
    }

    @Transactional
    public BookEntity randomBook() {
        var fb = faker.book();
        var random = new Random();

        return createBook(
                randomIsbn13(),
                randomIsbn10(),
                fb.title(),
                fb.title(),
                authors.isEmpty() ? Set.of() : Set.of(authors.get(random.nextInt(authors.size()))),
                publishers.isEmpty() ? Set.of() : Set.of(publishers.get(random.nextInt(publishers.size()))),
                categories.isEmpty() ? Set.of() : Set.of(categories.get(random.nextInt(categories.size()))),
                languageService.findOrCreateLanguage("ENGLISH"),
                LocalDate.now(),
                faker.lorem().paragraph());
    }

    @Transactional
    public List<BookEntity> randomBooks(int count) {
        var books = new ArrayList<BookEntity>();
        for (int i = 0; i < count; i++) {
            books.add(randomBook());
        }
        return books;
    }

    public String randomIsbn13() {
        return faker.code().isbn13();
    }

    public String randomIsbn10() {
        return faker.code().isbn10();
    }

    @Transactional
    public List<AuthorEntity> randomAuthors(int count) {
        var authors = new ArrayList<AuthorEntity>();
        for (int i = 0; i < count; i++) {
            var author = createAuthorImpl(faker.name().lastName(), faker.name().firstName());
            authors.add(author);
        }
        return authors;
    }

    @Transactional
    public List<PublisherEntity> randomPublishers(int i) {
        var publishers = new ArrayList<PublisherEntity>();
        for (int j = 0; j < i; j++) {
            publishers.add(createPublisher(faker.book().publisher()));
        }
        return publishers;
    }

    @Transactional
    public List<CategoryEntity> randomCategories(int i) {
        var categories = new ArrayList<CategoryEntity>();
        for (int j = 0; j < i; j++) {
            categories.add(createCategory(faker.book().genre()));
        }
        return categories;
    }

    @Transactional
    public CategoryEntity createCategory(String name) {
        var category = categoryService.findOrCreateCategory(name);
        logger.debug("Category `{}` created", category.getCategoryName());
        return category;
    }

}

