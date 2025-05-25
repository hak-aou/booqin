package fr.uge.booqin.infra.persistence.fixtures;

import com.github.javafaker.Faker;
import fr.uge.booqin.app.service.book.ScheduledTasks;
import fr.uge.booqin.app.service.collection.CollectionService;
import fr.uge.booqin.infra.external.avatar.AvatarGenerator;
import fr.uge.booqin.infra.persistence.fixtures.comment.CommentFixtures;
import fr.uge.booqin.infra.persistence.repository.book.BookRepository;
import fr.uge.booqin.infra.persistence.repository.collection.CollectionRepository;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Component("Fixtures")
public class Fixtures {
    private static final Logger logger = LoggerFactory.getLogger(Fixtures.class);

    private final boolean fixturesEnabled;
    private final String activeProfile;
    private final Faker faker = new Faker();

    private final UserFixtures userFixtures;
    private final CollectionFixtures collectionFixtures;
    private final CommentFixtures commentFixtures;
    private final AvatarGenerator avatarGenerator;
    private final BookRepository bookRepository;
    private final boolean fetchBooksFromApi;
    private final CollectionService collectionService;
    private final CollectionRepository collectionRepository;
    private final ScheduledTasks scheduledTasks;

    public Fixtures(UserFixtures userFixtures,
                    CommentFixtures commentFixtures,
                    AvatarGenerator avatarGenerator,
                    @Value("${spring.profiles.active}") String activeProfile,
                    @Value("${booqin.fixtures.enabled:}") boolean fixturesEnabled,
                    @Value("${booqin.fetchBooksFromApi:}") boolean fetchBooksFromApi,
                    CollectionFixtures collectionFixtures,
                    BookRepository bookRepository,
                    CollectionService collectionService,
                    CollectionRepository collectionRepository,
                    ScheduledTasks scheduledTasks
                    ) {
        this.userFixtures = userFixtures;
        this.avatarGenerator = avatarGenerator;
        this.collectionFixtures = collectionFixtures;
        this.commentFixtures = commentFixtures;
        this.bookRepository = bookRepository;
        this.collectionService = collectionService;
        this.activeProfile = activeProfile;
        this.fixturesEnabled = fixturesEnabled;
        this.fetchBooksFromApi = fetchBooksFromApi;
        this.collectionRepository = collectionRepository;
        this.scheduledTasks = scheduledTasks;
    }

    @PostConstruct
    @Transactional
    public void init() throws IOException {
        prerequisites();
        logger.info("Active profile: '{}'", activeProfile);
        if (!fixturesEnabled) {
            logger.info("Skipping fixtures as the property 'booqin.fixtures.enabled' is set to false");
            return;
        }
        createDataset();
        postRequisites();
    }

    @Transactional
    public void prerequisites() throws IOException {
        // system identity
        var booqInIdentity = userFixtures.createIdentity(
                faker.internet().password(99, 100, true, true, true));
        if(booqInIdentity == null) {
            throw new AssertionError("booqInIdentity is null");
        }
        var booqIn = userFixtures.createUser(booqInIdentity,
                true,
                "booqin",
                "admin@booqin.social",
                "https://api.dicebear.com/9.x/initials/svg?seed=B&radius=50&backgroundColor=00897B&fontFamily=Verdana&fontSize=80",
                Instant.now()
        );
        // system collections
        //Anne Reeve Aldrich
        collectionFixtures.globalCollection(booqIn);
        if(fetchBooksFromApi) {
            scheduledTasks.updateDatabaseFromApi();
        } else {
            logger.info("Skipping book fetching from API as the property 'booqin.fetchBooksFromApi' is set to false");
        }
    }

    @Transactional
    public void postRequisites() {
        scheduledTasks.createRecentBooksCollection();
        scheduledTasks.updateAllSmartCollection();
        scheduledTasks.creationOneCollectionPerAuthor();
    }

    @Transactional
    public void createDataset() {
        logger.info("---(Running fixtures)---");

        ////// AUTH IDENTITIES
        var adminIdentity = userFixtures.createAdminAuthIdentity();
        var userIdentity = userFixtures.createUserAuthIdentity();
        var antoineIdentity = userFixtures.createIdentity("antoine");
        var hakimIdentity = userFixtures.createIdentity("hakim");
        var testIdentity = userFixtures.createIdentity("test");
        logger.info("auth identities created");

        ////// USERS
        var admin = userFixtures.createUser(adminIdentity,
                true,
                "admin123",
                "admin@me.com",
                avatarGenerator.generateAvatar("adminAvatarSeed"),
                Instant.now());
        var user = userFixtures.createUser(userIdentity,
                false,
                "user123",
                "user@me.com",
                avatarGenerator.generateAvatar("userAvatarSeed"),
                Instant.now());
        var antoine = userFixtures
                .createUser(antoineIdentity, true,
                        "antoine", "antoine@me.com",
                        "https://api.dicebear.com/9.x/notionists/svg?seed=Adrian&flip=true&scale=130" +
                                "&radius=50&backgroundType[]&beard=variant05&beardProbability=100" +
                                "&body=variant06&bodyIcon[]&bodyIconProbability=100&brows=variant08" +
                                "&eyes=variant05&gesture[]&gestureProbability=100" +
                                "&glasses=variant11&glassesProbability=100&hair=variant15&lips=variant14" +
                                "&nose=variant10,variant07&backgroundColor=ffdfbf",
                        Instant.now());
        var hakim = userFixtures
                .createUser(hakimIdentity, true,
                        "hakim", "hakim@me.com",
                        "https://api.dicebear.com/9.x/open-peeps/svg?seed=fea",
                        Instant.now());

        var test = userFixtures
                .createUser(testIdentity, false,
                        "test", "test@mail.me",
                        "https://api.dicebear.com/9.x/bottts-neutral/svg?seed=Ryan",
                        Instant.now());

        var dummyUsers = userFixtures.createDummyUsers(5);
        logger.info("users created");

        ////// Follows
        userFixtures.makeFollow(admin, user);
        dummyUsers.forEach(dummy -> userFixtures.makeFollow(dummy, user));
        dummyUsers.forEach(dummy -> userFixtures.makeFollow(dummy, antoine));
        dummyUsers.forEach(dummy -> userFixtures.makeFollow(dummy, hakim));
        dummyUsers.forEach(dummy -> userFixtures.makeFollow(dummy, test));
        logger.info("follows created");

        ////// Collections
        // Create a collection for users
        var collections = List.of(
                collectionFixtures.randomCollectionFor(admin),
        collectionFixtures.randomCollectionFor(user),
        collectionFixtures.randomCollectionFor(antoine),
        collectionFixtures.randomCollectionFor(antoine,false),
        collectionFixtures.randomCollectionFor(hakim),
        collectionFixtures.randomCollectionFor(test));

        logger.info("collections created");

        var globalCollection = collectionRepository.findAllByVisibility(true)
                .stream()
                .filter(c -> c.getTitle().equals("Books"))
                .toList()
                .getFirst();

        // var allBooksNotInGlobalCollection = bookRepository.findAllNotInGlobalCollection();
        var booksNotInGlobalCollection = collections.stream().flatMap(c -> c.getBooks().stream()).toList();
        collectionService.addAllBooksEntitiesToCollectionSystem(globalCollection.getId(), booksNotInGlobalCollection);
        ////// Comments
        var comment = commentFixtures.randomComment(admin, globalCollection.getCommentable().getId());
        var reply = commentFixtures.replyRandomComment(user, comment);
        var reply2 = commentFixtures.replyRandomComment(dummyUsers.getFirst(), reply);
        var reply3 = commentFixtures.replyRandomComment(dummyUsers.get(2), reply2);
        commentFixtures.replyRandomComment(dummyUsers.get(3), reply3);
        logger.info("comments created");
        //smartCollectionFixtures.collectionByEachAuthor(booqIn);
        //logger.info("smart collections created");
    }

}
