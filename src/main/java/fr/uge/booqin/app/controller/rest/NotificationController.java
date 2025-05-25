package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.notification.UserNotificationsDTO;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.service.AuthService;
import fr.uge.booqin.app.service.notification.NotificationService;
import fr.uge.booqin.app.service.notification.NotificationSseEmitterService;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import fr.uge.booqin.infra.security.auth.jwt.JwtException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

///
/// Controller for notifications
///
/// Using Server-Sent Events (SSE) to push notifications to the client
///
/// Flow:
/// - first the client must request a token (valid 1 min) to the /token endpoint
/// - then the client can subscribe to the subscribe/{token} endpoint
@RestController
@RequestMapping({"/api/notifications", "/android/notifications"})
public class NotificationController {

    private final AuthService authService;
    private final NotificationSseEmitterService sseService;
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(AuthService authService,
                                  NotificationSseEmitterService sseService,
                                  NotificationService notificationService,
                                  UserService userService) {
        this.authService = authService;
        this.sseService = sseService;
        this.notificationService = notificationService;
        this.userService = userService;
    }

    /**
     * Generate a temporary token to authenticate to the SSE endpoint.
     * (SSE does not support cookies or Bearer tokens)
     * @param refreshToken the refresh token, stored in a http-only cookie
     * @return a token to use in the subscribe endpoint
     */
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateSseToken(@CookieValue(name = "refresh_token") String refreshToken) {
        if (refreshToken == null || refreshToken.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String accessToken;
        try {
            accessToken = authService.quickToken(
                    refreshToken,
                    (authenticatedUser -> userService.findMyProfile(authenticatedUser).publicProfileDto().id().toString())
            );
        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("token", accessToken));
    }

    public record AndroidTokenRequest(String refreshToken) {}

    @PostMapping("/androidToken")
    public ResponseEntity<Map<String, String>> generateSseTokenForAndroid(@RequestBody AndroidTokenRequest request) {
        return generateSseToken(request.refreshToken);
    }

    @GetMapping("/subscribe/{token}")
    public SseEmitter subscribe(@PathVariable String token) {
        var userId = UUID.fromString(authService.validateAndGetSubject(token));
        return sseService.openSseConnection(userId);
    }

    @PostMapping
    public UserNotificationsDTO getNotifications(
            @AuthenticationPrincipal SecurityUser currentUser,
            @RequestBody PageRequest pageRequest) {
        return notificationService.getUserNotifications(currentUser.authenticatedUser(), pageRequest);
    }

    public record NotificationDeleteRequest(List<UUID> notificationId) {}

    @DeleteMapping("")
    public void deleteAllNotifications(@AuthenticationPrincipal SecurityUser currentUser, @RequestBody NotificationDeleteRequest request) {
        notificationService.deleteAllNotifications(request.notificationId(), currentUser.authenticatedUser());
    }
}