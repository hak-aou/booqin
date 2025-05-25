package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.user.PrivateProfileDTO;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.app.dto.user.RegisterRequestDTO;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/user", "/android/user"})
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public PrivateProfileDTO me(@AuthenticationPrincipal SecurityUser currentUser) {
        return userService.findMyProfile(currentUser.authenticatedUser());
    }

    @GetMapping("/{userId}")
    public PublicProfileDTO user(@PathVariable UUID userId) {
        return userService.findPublicProfile(userId);
    }

    @PostMapping("")
    public void register(@RequestBody RegisterRequestDTO request) {
        userService.register(request);
    }
}