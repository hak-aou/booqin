package fr.uge.booqin.infra.persistence.fixtures;

import com.github.javafaker.Faker;
import fr.uge.booqin.infra.persistence.entity.collection.standard.CollectionEntity;
import fr.uge.booqin.infra.persistence.entity.book.BookEntity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.collection.CollectionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
public class CollectionFixtures {
    private static final Logger logger = LoggerFactory.getLogger(CollectionFixtures.class);
    private final Faker faker = new Faker();
    private final CollectionRepository collectionRepository;
    private final BookFixtures bookFixtures;
    private final Random random = new Random();

    public CollectionFixtures(CollectionRepository collectionRepository, BookFixtures bookFixtures) {
            this.collectionRepository = collectionRepository;
            this.bookFixtures = bookFixtures;
    }

    @Transactional
    public CollectionEntity createUserCollection(String title, String description, boolean visibility,
                                                  UserEntity user, List<BookEntity> books) {
        var collection = new CollectionEntity(title, description, visibility, user);
        collection.addAllBooks(books);
        var savedEntity = collectionRepository.save(collection);
        logger.debug("UserCollection `{}` created", savedEntity);
        return savedEntity;
    }

    @Transactional
    public void addBookToCollection(Long collectionId, BookEntity book){
        var collection = collectionRepository.findByIdWithBooks(collectionId)
                .orElseThrow(() -> new NoSuchElementException("Collection not found"));
        collection.addBook(book);
        logger.debug("added `{}` to collection {}", book.getTitle(), collection.getTitle());
        collectionRepository.save(collection);
    }

    @Transactional
    public CollectionEntity randomCollectionFor(UserEntity user) {
        return randomCollectionFor(user, random.nextBoolean());
    }

    @Transactional
    public CollectionEntity randomCollectionFor(UserEntity user, boolean visibility) {
        return createUserCollection(
                "My collection of " + faker.book().genre() ,
                faker.lorem().sentence(),
                visibility,
                user,
                bookFixtures.randomBooks(random.nextInt(20)));
    }

    @Transactional
    public CollectionEntity globalCollection(UserEntity booqIn) {
        var collection = collectionRepository.findByOwnerIdAndTitle(booqIn.getId(), "Books");
        // don't do lambda here, crucial. It will break the transaction
        // (seems to create a new EntityManager -> we lose the cache -> we can't see the collection)
        if (collection.isPresent()) {
            return collection.get();
        }
        return createUserCollection(
                "Books",
                "All the books",
                true,
                booqIn,
                List.of());
    }
}
