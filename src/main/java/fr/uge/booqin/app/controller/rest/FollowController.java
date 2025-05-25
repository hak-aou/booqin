package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.follow.FollowRelationshipDTO;
import fr.uge.booqin.app.dto.follow.FollowersRequest;
import fr.uge.booqin.app.dto.follow.FollowingsRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.app.service.follow.FollowService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/follow", "/android/follow"})
public class FollowController {

    private final FollowService followService;

    public FollowController(FollowService followService) {
        this.followService = followService;
    }

    @PostMapping("/followers")
    public PaginatedResult<PublicProfileDTO> followers(@RequestBody FollowersRequest followersRequest) {
        return followService.followersOfFollowable(followersRequest);
    }

    @PostMapping("/followings")
    public PaginatedResult<PublicProfileDTO> followings(@RequestBody FollowingsRequest followingsRequest) {
        return followService.followingsOfUser(followingsRequest);
    }

    @PostMapping("/{followableId}")
    public void followFollowable(@PathVariable UUID followableId, @AuthenticationPrincipal SecurityUser currentUser) {
        followService.follow(currentUser.authenticatedUser(), followableId);
    }

    @DeleteMapping("/{followableId}")
    public void unfollow(@PathVariable UUID followableId, @AuthenticationPrincipal SecurityUser currentUser) {
        followService.unfollow(currentUser.authenticatedUser(), followableId);
    }

    @PostMapping("/relationship/{followableId}")
    public FollowRelationshipDTO relationship(@PathVariable UUID followableId, @AuthenticationPrincipal SecurityUser currentUser) {
        return followService.relationshipWithFollowable(currentUser.authenticatedUser(), followableId);
    }

    // Special case for the follow feature, notifications and so on //

    @PostMapping("/users/{followableIdOfUser}")
    public void followUser(@PathVariable UUID followableIdOfUser, @AuthenticationPrincipal SecurityUser currentUser) {
        followService.followUser(currentUser.authenticatedUser(), followableIdOfUser);
    }

}
