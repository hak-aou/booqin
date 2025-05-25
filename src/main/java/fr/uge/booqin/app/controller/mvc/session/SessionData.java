package fr.uge.booqin.app.controller.mvc.session;

import fr.uge.booqin.app.dto.user.PrivateProfileDTO;
import fr.uge.booqin.domain.model.User;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionData implements Serializable {

    private User user;
    private PrivateProfileDTO profile;

    public User user() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLogged() {
        return user != null;
    }

    public void setProfile(PrivateProfileDTO myProfile) {
        this.profile = myProfile;
    }

    public PrivateProfileDTO profile() {
        return profile;
    }
}