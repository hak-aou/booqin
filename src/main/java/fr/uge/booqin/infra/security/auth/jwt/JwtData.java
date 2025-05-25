package fr.uge.booqin.infra.security.auth.jwt;

public record JwtData(String accessToken, String refreshToken) { }