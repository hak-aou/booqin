package fr.uge.booqin.app.controller.interceptor;

import fr.uge.booqin.domain.model.exception.BooqInException;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.infra.security.auth.jwt.JwtException;
import fr.uge.booqin.infra.security.exception.AuthenticationException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.util.HashMap;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(JwtException.class)
    public ResponseEntity<String> handleJwtException(JwtException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<String> handleAuthenticationException(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                             .body(ex.getMessage());
    }

    @ExceptionHandler(SQLException.class)
    public ResponseEntity<?> handleSqlException(SQLException __) {
        return handleBooqInException(new TheirFaultException("invalid request"));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException ex) {
        return handleBooqInException(new TheirFaultException("invalid request"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException ex) {
        return handleBooqInException(new TheirFaultException(ex.getMessage()));
    }

    @ExceptionHandler(BooqInException.class)
    public ResponseEntity<?> handleBooqInException(BooqInException ex) {
        var httpStatus = switch(ex) {
            case OurFaultException __ -> HttpStatus.INTERNAL_SERVER_ERROR;
            case TheirFaultException __ -> HttpStatus.BAD_REQUEST;
        };
        var error = new HashMap<String, Object>();
        error.put("error", ex.getMessage() == null ? "Unknown error" : ex.getMessage());
        error.put("reasons", ex.reasons());
        return ResponseEntity.status(httpStatus).body(error);
    }

    @ExceptionHandler(MissingRequestCookieException.class)
    public ResponseEntity<?> handleMissingRequestCookieException(MissingRequestCookieException ex) {
        return handleBooqInException(new TheirFaultException(ex.getMessage()));
    }
}