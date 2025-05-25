package fr.uge.booqin.domain.model.config;

import java.time.Duration;
import java.util.Objects;

public interface AuthConfig {
    Duration accessTokenExpiration();
    Duration refreshTokenExpiration();

    Duration trustedDeviceTokenExpiration();

    class AuthConfigBuilder {
        private Duration accessTokenExpiration;
        private Duration refreshTokenExpiration;
        private Duration trustedDeviceTokenExpiration;

        public AuthConfigBuilder accessTokenExpiration(Duration accessTokenExpiration) {
            this.accessTokenExpiration = Objects.requireNonNull(accessTokenExpiration);
            return this;
        }

        public AuthConfigBuilder refreshTokenExpiration(Duration refreshTokenExpiration) {
            this.refreshTokenExpiration = Objects.requireNonNull(refreshTokenExpiration);
            return this;
        }

        public AuthConfigBuilder trustedDeviceTokenExpiration(Duration trustedDeviceTokenExpiration) {
            this.trustedDeviceTokenExpiration = Objects.requireNonNull(trustedDeviceTokenExpiration);
            return this;
        }

        public AuthConfig build() {
            if (accessTokenExpiration == null) {
                throw new IllegalArgumentException("accessTokenExpiration must not be null");
            }
            if (refreshTokenExpiration == null) {
                throw new IllegalArgumentException("refreshTokenExpiration must not be null");
            }
            if (accessTokenExpiration.isNegative()) {
                throw new IllegalArgumentException("accessTokenExpiration must not be negative");
            }
            if (refreshTokenExpiration.isNegative()) {
                throw new IllegalArgumentException("refreshTokenExpiration must not be negative");
            }
            if (trustedDeviceTokenExpiration == null) {
                throw new IllegalArgumentException("trustedDeviceTokenExpiration must not be null");
            }
            if (trustedDeviceTokenExpiration.isNegative()) {
                throw new IllegalArgumentException("trustedDeviceTokenExpiration must not be negative");
            }

            return new AuthConfig() {
                @Override
                public Duration accessTokenExpiration() {
                    return accessTokenExpiration;
                }

                @Override
                public Duration refreshTokenExpiration() {
                    return refreshTokenExpiration;
                }

                @Override
                public Duration trustedDeviceTokenExpiration() {
                    return trustedDeviceTokenExpiration;
                }
            };
        }
    }
}
