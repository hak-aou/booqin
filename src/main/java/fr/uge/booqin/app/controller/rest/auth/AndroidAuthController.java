package fr.uge.booqin.app.controller.rest.auth;

import fr.uge.booqin.app.dto.auth.AccessToken;
import fr.uge.booqin.app.dto.auth.AuthRequest;
import fr.uge.booqin.app.dto.auth.RefreshRequest;
import fr.uge.booqin.app.service.AuthService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import javax.naming.AuthenticationException;
import java.util.Map;

@RestController
@RequestMapping( "/android/auth")
public class AndroidAuthController {
    private final AuthService authService;
    public AndroidAuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        var jwtData = authService.authenticate(authRequest);
        return ResponseEntity.ok(jwtData);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AccessToken> refresh(@RequestBody RefreshRequest request) throws AuthenticationException {
        var refreshToken = request.refreshToken();
        var newToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new AccessToken(newToken));
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
