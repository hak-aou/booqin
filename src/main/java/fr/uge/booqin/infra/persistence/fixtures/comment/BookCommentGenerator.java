package fr.uge.booqin.infra.persistence.fixtures.comment;

import com.github.javafaker.Faker;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

@Component
public class BookCommentGenerator {
    private final Faker faker;
    private final Random random;

    private final List<String> bookPhrases = Arrays.asList(
            "couldn't put it down",
            "page-turner",
            "beautifully written",
            "compelling characters",
            "thought-provoking",
            "must-read",
            "masterpiece",
            "deeply moving"
    );

    private final List<String> positiveAdjectives = Arrays.asList(
            "amazing", "brilliant", "captivating", "delightful",
            "engaging", "fascinating", "gripping", "impressive",
            "masterful", "outstanding", "remarkable", "superb"
    );

    private final List<String> emotions = Arrays.asList(
            "inspired", "moved", "touched", "entertained",
            "enlightened", "uplifted", "transported", "intrigued"
    );

    public BookCommentGenerator() {
        this.faker = new Faker();
        this.random = new Random();
    }

    private String generateShortComment() {
        return String.format("%s %s!",
                faker.lorem().sentence(4, 4).replace(".", ""),
                bookPhrases.get(random.nextInt(bookPhrases.size()))
        );
    }

    private String generateEmotionalComment() {
        return String.format("I %s this book! %s The characters felt so %s.",
                random.nextBoolean() ? "loved" : "really enjoyed",
                faker.lorem().sentence(6, 4),
                emotions.get(random.nextInt(emotions.size()))
        );
    }

    private String generateCriticalComment() {
        return String.format("The %s was %s, although the %s could have been better. %s",
                random.nextBoolean() ? "plot" : "character development",
                positiveAdjectives.get(random.nextInt(positiveAdjectives.size())),
                random.nextBoolean() ? "pacing" : "ending",
                faker.lorem().sentence(4, 4)
        );
    }

    private String generateRecommendationComment() {
        return String.format("If you enjoy %s, you'll love this book. %s",
                faker.book().genre(),
                faker.lorem().sentence(5, 4)
        );
    }

    private String generateDetailedComment() {
        return String.format("%s %s The author really %s. %s",
                faker.lorem().sentence(6, 4),
                bookPhrases.get(random.nextInt(bookPhrases.size())),
                random.nextBoolean() ?
                        "brings the characters to life" :
                        "captures the essence of the story",
                faker.lorem().sentence(4, 4)
        );
    }

    public List<String> generateComments(int count) {
        var comments = new String[count];
        for (int i = 0; i < count; i++) {
            comments[i] = generateComment();
        }
        return Arrays.asList(comments);
    }

    public String generateComment() {
        var commentsSuppliers = new Supplier[] {
                this::generateShortComment,
                this::generateEmotionalComment,
                this::generateCriticalComment,
                this::generateRecommendationComment,
                this::generateDetailedComment
        };
        return (String) commentsSuppliers[random.nextInt(commentsSuppliers.length)].get();
    }
}