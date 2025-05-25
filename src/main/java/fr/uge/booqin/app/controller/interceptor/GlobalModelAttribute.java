package fr.uge.booqin.app.controller.interceptor;

import fr.uge.booqin.app.controller.mvc.session.SessionData;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.config.NonceFilter;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Add global model attributes
 */
@ControllerAdvice
public class GlobalModelAttribute {

    private final SessionData sessionData;

    public GlobalModelAttribute(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    /*
       https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html#csrf-integration-javascript-other
        csrf token is not included by default in JTE
     */
    @ModelAttribute("_csrf")
    public CsrfToken addCsrfToken(CsrfToken csrfToken) {
        return csrfToken;
    }

    /*
        https://docs.spring.io/spring-security/site/docs/current/reference/html5/#mvc-authentication-principal
        add current authenticated user to model (if any)
     */
    @ModelAttribute("currentUser")
    public User addCurrentUser(@AuthenticationPrincipal SecurityUser currentUser) {
        return currentUser == null ? null : currentUser.authenticatedUser();
    }

    @ModelAttribute("session")
    public SessionData addSessionData() {
        return sessionData;
    }

    @ModelAttribute("nonce")
    public String addNonce(HttpServletRequest request) {
        return (String) request.getAttribute(NonceFilter.NONCE_ATTRIBUTE);
    }

    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<String> handleUsernameNotFoundException(UsernameNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
    }
}