package fr.uge.booqin.app.service.book;

import fr.uge.booqin.app.dto.book.BookInfoDTO;
import fr.uge.booqin.app.dto.book.BookMapper;
import fr.uge.booqin.app.dto.filter.FilterBooksDTO;
import fr.uge.booqin.app.service.loan.bookstock.BookStockManager;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.books.BookModel;
import fr.uge.booqin.domain.model.books.IsbnModel;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.external.book.adapter.BookAPI;
import fr.uge.booqin.infra.external.book.query_parameter.GoogleBookQueryParameterBuilder;
import fr.uge.booqin.infra.external.book.query_parameter.OpenLibraryQueryParameterBuilder;
import fr.uge.booqin.infra.external.book.query_parameter.QueryLanguage;
import fr.uge.booqin.infra.persistence.entity.book.*;
import fr.uge.booqin.infra.persistence.entity.collection.smart.filter.SmartCollectionFilterEntity;
import fr.uge.booqin.infra.persistence.repository.book.*;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import fr.uge.booqin.infra.external.book.adapter.GoogleBooksAPIAdapter;
import fr.uge.booqin.infra.external.book.adapter.OpenLibraryAdapter;

import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Stream;


@Service
public class BookService {


    private final WebClient webClient;

    private final BookRepository bookRepository;

    private final AuthorService authorService;
    private final PublisherService publisherService;
    private final LanguageService languageService;
    private final CategoryService categoryService;
    private final BookStockManager bookStockManager;
    private final BooqInConfig config;

    public BookService(
                        BooqInConfig booqInConfig,
                        BookRepository bookRepository,
                       AuthorService authorService,
                       PublisherService publisherService,
                       LanguageService languageService,
                       CategoryService categoryService,
                       BookStockManager bookStockManager,
                       WebClient webClient) {
        this.config = booqInConfig;
        this.bookRepository = bookRepository;
        this.authorService = authorService;
        this.publisherService = publisherService;
        this.languageService = languageService;
        this.categoryService = categoryService;
        this.bookStockManager = bookStockManager;
        this.webClient = webClient;
    }

    private BookEntity fromModeltoEntity(BookModel bookModel) {
        return new BookEntity(
                bookModel.isbn().isbn_13(),
                bookModel.isbn().isbn_10(),
                bookModel.title(),
                bookModel.authors().stream().map(authorService::findOrCreateAuthor).toList(),
                bookModel.publishers().stream().map(publisherService::findOrCreatePublisher).toList(),
                bookModel.publishedDate(),
                bookModel.categories().stream().map(categoryService::findOrCreateCategory).toList(),
                languageService.findOrCreateLanguage(bookModel.language()),
                bookModel.imageLinks(),
                bookModel.subtitle(),
                bookModel.description(),
                bookModel.pageCount()
        );
    }

    @Transactional
    public List<BookEntity> insertBooks(List<BookModel> booksModel) {
        var booksEntity = booksModel.stream()
                .filter(Objects::nonNull)
                .map(this::fromModeltoEntity)
                .peek(bookEntity -> bookEntity.setAddedAt(Instant.now()))
                .toList();
        return bookRepository.saveAll(booksEntity);
    }

    public List<BookModel> getBooksFromAuthorsFile(String path, int limit) throws IOException {
        List<BookModel> books = new ArrayList<>();
        int nb = 0;

        StringBuilder sbLogger = new StringBuilder();
        sbLogger.append("Start fetching apis : ")
                .append(LocalDate.now()).append(" at ").append(Instant.now()).append("\n");

        File inputFile = new File(path);
        File tempFile = new File(path + ".tmp");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))
        ) {
            String line;
            while ((line = reader.readLine()) != null) {
                if(nb >= limit) {
                    writer.write(line);
                    writer.newLine();
                } else {
                    String author = line.trim();
                    var mergedBooks = getBooksForAuthor(author);
                    books.addAll(mergedBooks);
                    logAuthorBooks(sbLogger, author, mergedBooks); // logger
                }
                nb++;
            }

        } catch (Exception e) {
            throw new OurFaultException("Error while reading book from author : " + e.getMessage());
        } finally {
            Files.delete(inputFile.toPath());
            Files.move(tempFile.toPath(), inputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            sbLogger.append("End fetching apis :").append(LocalDate.now()).append("\n\n");
            writeLogger("api_log.txt", sbLogger.toString());
        }

        return books;
    }



    private void logAuthorBooks(StringBuilder logger, String author, List<BookModel> books) {
        logger.append("\t-Author : ").append(author).append("\n");
        logger.append("\t-Books : ").append(books.size()).append("\n");
        for (BookModel book : books) {
            logger.append("\t\tisbn13 : ").append(book.isbn().isbn_13()).append("\n");
        }
    }

    private void writeLogger(String fileName, String content) {
        var filePath = Paths.get(config.logPath(), fileName);

        try (var writer = new BufferedWriter(new FileWriter(filePath.toFile(), true))) {
            writer.write(content);
        } catch (IOException e) {
            throw new OurFaultException("Error while writing log file : " + e.getMessage());
        }
    }

    private List<BookModel> getBooksForAuthor(String author) {
        BookAPI googleBooksAPI = new GoogleBooksAPIAdapter(webClient);
        BookAPI openLibraryAPI = new OpenLibraryAdapter(webClient);

        var googleQuery = new GoogleBookQueryParameterBuilder()
                .author(author)
                .language(QueryLanguage.ENGLISH);
        var openLibraryQuery = new OpenLibraryQueryParameterBuilder()
                .author(author)
                .language(QueryLanguage.ENGLISH);

        var googleBooks = googleBooksAPI.getBookMetadata(googleQuery);
        var openLibraryBooks = openLibraryAPI.getBookMetadata(openLibraryQuery);

        return mergeBookModels(googleBooks, openLibraryBooks);
    }

    @Transactional
    public BookModel findBookByIsbn13(String isbn13) {
        var bookEntity = bookRepository.findBooksByIsbn13(isbn13);

        if (bookEntity.isEmpty()) {
            throw new OurFaultException("Book with isbn " + isbn13 + " do not exist in our database");
        }
        return BookMapper.fromEntityToModel(bookEntity.get());
    }

    @Transactional
    public BookModel findBookByIdToModel(UUID bookId) {
        var bookEntity = bookRepository.findById(bookId);
        if (bookEntity.isEmpty()) {
            throw new TheirFaultException("Book with id " + bookId + " not found");
        }
        return BookMapper.fromEntityToModel(bookEntity.get());
    }

    @Transactional
    public BookInfoDTO findBookById(UUID bookId) {
        var bookEntity = bookRepository.findById(bookId)
                .orElseThrow(() -> new TheirFaultException("Book with id " + bookId + " not found"));
        return from(bookEntity);
    }

    private List<BookModel> mergeBookModels(List<BookModel>... listApiBooks) {
        Map<String, BookModel> mergedBooks = new HashMap<>();

        for (List<BookModel> apiBooks : listApiBooks) {
            for (BookModel book : apiBooks) {
                var currentBook = mergedBooks.get(book.isbn().isbn_13());
                if (currentBook != null) {
                    mergedBooks.put(book.isbn().isbn_13(), mergeBookModel(currentBook, book));
                } else {
                    mergedBooks.put(book.isbn().isbn_13(), book);
                }
            }
        }

        return mergedBooks.values().stream().toList();
    }

    private BookModel mergeBookModel(BookModel book1, BookModel book2) {
        return new BookModel(
                book1.votableId(),
                Optional.ofNullable(book1.title()).orElse(book2.title()),
                null, // @Todo to corriger
                null,
                null,
                Optional.ofNullable(book1.isbn()).orElse(book2.isbn()),
                mergeLists(book1.authors(), book2.authors()),
                mergeLists(book1.publishers(), book2.publishers()),
                Optional.ofNullable(book1.publishedDate()).orElse(book2.publishedDate()),
                mergeLists(book1.categories(), book2.categories()),
                Optional.ofNullable(book1.language()).orElse(book2.language()),
                Optional.ofNullable(book1.imageLinks()).orElse(book2.imageLinks()),
                Optional.ofNullable(book1.subtitle()).orElse(book2.subtitle()),
                Optional.ofNullable(book1.description()).orElse(book2.description()),
                Optional.ofNullable(book1.pageCount()).orElse(book2.pageCount())
        );
    }

    private static <T> List<T> mergeLists(List<T> list1, List<T> list2) {
        return Stream.concat(list1.stream(), list2.stream())
                .distinct().toList();
    }

    private Specification<BookEntity> booksFromCollectionId(Long collectionId) {
        return (root, query, builder) ->
                builder.equal(root.join("collections").get("id"), collectionId);
    }


    private Specification<BookEntity> specificationsFromFilter(FilterBooksDTO filterBook) {
        Specification<BookEntity> spec = Specification.where(null);

        if(filterBook == null) {
            return spec;
        }

        var filters = filterBook.convertTo();

        for (SmartCollectionFilterEntity filter : filters) {
            var filterModel = filter.convertTo();
            spec = spec.and(filterModel.toSpecification());
        }

        return spec;
    }

    // Use specification to avoid making our own sql query with the filters
    public Page<BookEntity> findBooksInCollectionWithFilters(Long collectionId,
                                                             FilterBooksDTO filterBook,
                                                             Pageable pageable) {
        var spec = specificationsFromFilter(filterBook)
                .and(booksFromCollectionId(collectionId));

        return bookRepository.findAll(spec, pageable);
    }

    public Page<BookEntity> findAllBooksWithFilters(FilterBooksDTO filterBook,
                                                 Pageable pageable) {
        var spec = specificationsFromFilter(filterBook);

        return bookRepository.findAll(spec, pageable);
    }

    @Transactional
    public Page<BookEntity> findFilteredBooksFromListBooks(FilterBooksDTO filterBook, Pageable pageable, List<BookEntity> books) {
        var spec = specificationsFromFilter(filterBook)
                .and((root, query, builder) -> root.in(books));

        return bookRepository.findAll(spec, pageable);
    }

    public List<String> getAllLanguages() {
        return languageService.getAllLanguages();
    }

    // TODO : must be deleted only to test
    public List<IsbnModel> getAllIsbns() {
        return bookRepository.findAll().stream()
                .map(bookEntity -> new IsbnModel(bookEntity.getIsbn13(), bookEntity.getIsbn10()))
                .toList();
    }

    public List<String> getAllCategories() {
        return categoryService.getAllCategories();
    }

    public BookInfoDTO from(BookEntity book) {
        return from(bookStockManager, book);
    }

    public static BookInfoDTO from(BookStockManager bookStockManager, BookEntity book) {
        return new BookInfoDTO(
                book.getId(),
                book.getCommentable().getId(),
                book.getFollowable().getId(),
                new IsbnModel(book.getIsbn13(), book.getIsbn10()),
                book.getTitle(),
                book.getSubtitle(),
                book.getCategories().stream().map(CategoryEntity::getCategoryName).toList(),
                book.getImageLinks(),
                bookStockManager.supply(book.getId())
        );
    }

    @Transactional
    public List<BookEntity> getRecentBooks(int days) {
        return bookRepository.findBookEntitiesByAddedAtAfter(Instant.now().minus(days, ChronoUnit.DAYS));
    }

    @Transactional
    public Optional<BookEntity> findBookByVotableId(UUID votableId) {
        return bookRepository.findBookEntityByVotable_Id(votableId);
    }

    @Transactional
    public List<BookEntity> findBooksByAuthor(String author) {
        return bookRepository.findBookEntitiesByAuthors_Name(author);
    }

}