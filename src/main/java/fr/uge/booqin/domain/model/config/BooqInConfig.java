package fr.uge.booqin.domain.model.config;


import fr.uge.booqin.domain.model.cart.PriceEstimator;
import fr.uge.booqin.domain.model.rules.UsernameValidator;
import fr.uge.booqin.domain.model.rules.comment.CommentSanitizer;
import fr.uge.booqin.domain.model.rules.comment.CommentValidator;
import fr.uge.booqin.domain.model.rules.PassphraseValidator;

import java.time.Duration;

public interface BooqInConfig {
    int maxCommentLength();
    CommentSanitizer commentSanitizer();
    CommentValidator commentValidator();
    PassphraseValidator passphraseValidator();
    UsernameValidator usernameValidator();
    CollectionConfig collectionConfig();
    Duration borrowWaitingListLockDuration();
    Duration basketLockTimeout();
    PriceEstimator priceEstimator();
    String logPath();
    String authorsFile();
    interface CollectionConfig {
        int maxTitleLength();
        int maxDescriptionLength();
    }
    int numberAuthorToFetch();
    int fetchRecentBooksDays();
    int updateSmartCollectionDayToFetch();
    AuthConfig authConfig();
}
