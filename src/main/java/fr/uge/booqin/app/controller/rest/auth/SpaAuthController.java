package fr.uge.booqin.app.controller.rest.auth;

import fr.uge.booqin.app.dto.auth.AccessToken;
import fr.uge.booqin.app.dto.auth.AuthRequest;
import fr.uge.booqin.app.service.AuthService;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import fr.uge.booqin.infra.security.auth.jwt.JwtException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class SpaAuthController {

    private final AuthService authService;
    private final BooqInConfig config;

    public SpaAuthController(AuthService authService, BooqInConfig config) {
        this.authService = authService;
        this.config = config;
    }

    /**
     * Login endpoint for SPA clients
     * The client sends a POST request with the usernameOrEmail and password in the request body
     * The server validates the credentials and sends back an access token
     * The refresh token is sent as an HTTP-only cookie
     * @param authRequest The request body containing the usernameOrEmail and password
     * @return the access token
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest, HttpServletResponse response) {
        var authData = authService.authenticate(authRequest);
        // Web clients receive HTTP-only cookie for the refresh token
        var refreshCookie = ResponseCookie.from("refresh_token", authData.refreshToken())
                .httpOnly(true)
                .secure(false) // Should be set to true in production (HTTPS)
                .path("/")
                .maxAge(authRequest.trustedDevice() ? Duration.ofDays(30) : config.authConfig().refreshTokenExpiration())
                .domain("localhost")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(new AccessToken(authData.accessToken()));
    }

    // Refresh Access Token
    @PostMapping(value="/refresh", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String accessToken;
        try {
            accessToken = authService.refreshToken(refreshToken);
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(new AccessToken(accessToken));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        var refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false) // Should be set to true in production (HTTPS)
                .path("/")
                .maxAge(Duration.ZERO)
                .domain("localhost")
                .sameSite("Lax")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return ResponseEntity.ok(Map.of());
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal SecurityUser currentUser) {
        if (currentUser == null) {
            return ResponseEntity.ok(Map.of());
        }
        var authenticatedUser = currentUser.authenticatedUser();
        return ResponseEntity.ok(authenticatedUser);
    }

}