package fr.uge.booqin.app.dto.auth;

public record AuthRequest(String usernameOrEmail, String password, boolean trustedDevice) {}
