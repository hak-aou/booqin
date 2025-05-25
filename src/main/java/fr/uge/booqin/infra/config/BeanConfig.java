package fr.uge.booqin.infra.config;

import fr.uge.booqin.domain.model.cart.PriceEstimator;
import fr.uge.booqin.domain.model.cart.TransactionWorkflowResolver;
import fr.uge.booqin.domain.model.config.AuthConfig;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.domain.model.rules.UsernameValidator;
import fr.uge.booqin.domain.model.rules.comment.CommentSanitizer;
import fr.uge.booqin.domain.model.rules.comment.CommentValidator;
import fr.uge.booqin.domain.model.rules.PassphraseValidator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@Configuration
@EnableAsync
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class BeanConfig {

    private final Environment env;

    public BeanConfig(Environment env) {
        this.env = env;
    }

    @Bean
    public BooqInConfig booqInConfig(AuthConfig authConfig, @Value("${LOG_PATH}") String logPath) {
        return new BooqInConfig() {

            @Override
            public int maxCommentLength() {
                return env.getProperty("booqin.comment.max-length", Integer.class);
            }

            @Override
            public CommentSanitizer commentSanitizer() {
                return CommentSanitizer.useDefault();
            }

            @Override
            public CommentValidator commentValidator() {
                return CommentValidator.useDefault(maxCommentLength());
            }

            @Override
            public AuthConfig authConfig() {
                return authConfig;
            }

            @Override
            public CollectionConfig collectionConfig() {
                return new CollectionConfig() {
                    @Override
                    public int maxTitleLength() {
                        return env.getProperty("booqin.collection.title.max-length", Integer.class);
                    }

                    @Override
                    public int maxDescriptionLength() {
                        return env.getProperty("booqin.collection.description.max-length", Integer.class);
                    }
                };
            }

            @Override
            public Duration borrowWaitingListLockDuration() {
                return env.getProperty("booqin.borrow.waiting-list-lock-duration", Duration.class);
            }

            @Override
            public Duration basketLockTimeout() {
                return env.getProperty("booqin.cart.book-lock-timeout", Duration.class);
            }

            @Override
            public PriceEstimator priceEstimator() {
                return PriceEstimator.useDefault();
            }

            @Override
            public String logPath() {
                return logPath;
            }

            @Override
            public String authorsFile() {
                return env.getProperty("booqin.input.authors", String.class);
            }

            @Override
            public int numberAuthorToFetch() {
                return env.getProperty("booqin.bookapi.numberAuthorToFetch", Integer.class);
            }

            @Override
            public int fetchRecentBooksDays() {
                return env.getProperty("booqin.bookapi.fetchRecentBooksDays", Integer.class);
            }

            @Override
            public int updateSmartCollectionDayToFetch() {
                return env.getProperty("booqin.bookapi.updateSmartCollectionDayToFetch", Integer.class);
            }

            @Override
            public PassphraseValidator passphraseValidator() {
                return PassphraseValidator.useDefault(
                        env.getProperty("booqin.min-passphrase-length", Integer.class),
                        env.getProperty("booqin.max-passphrase-length", Integer.class)
                );
            }

            @Override
            public UsernameValidator usernameValidator() {
                return UsernameValidator.useDefault();
            }
        };
    }

    @Bean
    public TransactionWorkflowResolver transactionWorkflowResolver() {
        return TransactionWorkflowResolver.defaultResolver();
    }

    @Bean
    public AuthConfig authConfig() {
        var accessTokenExpiration = env.getProperty("jwt.access.expiration", Duration.class);
        var refreshTokenExpiration = env.getProperty("jwt.refresh.expiration", Duration.class);
        var trustedDeviceTokenExpiration = env.getProperty("jwt.trusted.expiration", Duration.class);
        return new AuthConfig.AuthConfigBuilder()
                .accessTokenExpiration(accessTokenExpiration)
                .refreshTokenExpiration(refreshTokenExpiration)
                .trustedDeviceTokenExpiration(trustedDeviceTokenExpiration)
                .build();
    }

    @Bean
    public static WebClient getWebClient(WebClient.Builder defaultBuilder) {
        return defaultBuilder.exchangeStrategies(ExchangeStrategies.builder()
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(16 * 1024 * 1024)).build()).build();
    }
}
