package fr.uge.booqin.domain.model;

import java.util.UUID;

public interface User {
    UUID id();
    String username();
    String email();
    UUID followableId();
    default boolean isAdmin() {
        return false;
    }

    static User of(UUID id, String username, String email, UUID followableId) {
        return new User() {
            @Override
            public UUID id() {
                return id;
            }

            @Override
            public String username() {
                return username;
            }

            @Override
            public String email() {
                return email;
            }

            @Override
            public UUID followableId() {
                return followableId;
            }
        };
    }
}
