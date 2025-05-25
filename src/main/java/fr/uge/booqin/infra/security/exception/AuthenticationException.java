package fr.uge.booqin.infra.security.exception;

public class AuthenticationException extends org.springframework.security.core.AuthenticationException {
    public AuthenticationException(String message) {
        super(message);
    }
}
