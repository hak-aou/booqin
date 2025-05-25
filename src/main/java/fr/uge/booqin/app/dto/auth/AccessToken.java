package fr.uge.booqin.app.dto.auth;

public record AccessToken(String accessToken) {
    public AccessToken {
        if (accessToken == null) {
            throw new IllegalArgumentException("Access token cannot be null");
        }
    }
}
