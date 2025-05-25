package fr.uge.booqin.infra.security.auth.jwt;

public class JwtException extends RuntimeException {
    public JwtException(String message) {
        super(message);
    }

    public static JwtException processingError() {
        return new JwtException("Error while processing JWT");
    }
}
