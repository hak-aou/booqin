package fr.uge.booqin.domain.model;

import java.util.UUID;

public interface Admin extends User {
    @Override
    default boolean isAdmin() {
        return true;
    }

    static Admin of(User user) {
        return new Admin() {
            @Override
            public UUID id() {
                return user.id();
            }

            @Override
            public String username() {
                return user.username();
            }

            @Override
            public String email() {
                return user.email();
            }

            @Override
            public UUID followableId() {
                return user.followableId();
            }
        };
    }


}
