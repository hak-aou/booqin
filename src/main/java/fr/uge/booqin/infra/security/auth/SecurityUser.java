package fr.uge.booqin.infra.security.auth;

import fr.uge.booqin.app.controller.interceptor.GlobalModelAttribute;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;


public class SecurityUser extends User {
    private final fr.uge.booqin.domain.model.User currentUser;

    public SecurityUser(String username, String password, Collection<? extends GrantedAuthority> authorities, fr.uge.booqin.domain.model.User currentUser) {
        super(username, password, authorities);
        this.currentUser = currentUser;
    }

    /**
     * Used by the {@link GlobalModelAttribute} to add the current authenticated user to the model
     * @return the current authenticated user
     */
    public fr.uge.booqin.domain.model.User authenticatedUser() {
        return currentUser;
    }
}
