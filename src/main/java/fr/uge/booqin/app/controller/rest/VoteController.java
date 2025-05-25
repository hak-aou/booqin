package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.vote.HasVoteDTO;
import fr.uge.booqin.app.service.vote.VoteService;
import fr.uge.booqin.infra.persistence.entity.vote.VoteType;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/vote", "/android/user"})
public class VoteController {

    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/upvote/{votableId}")
    public void upvote(@PathVariable UUID votableId,
                     @AuthenticationPrincipal SecurityUser currentUser) {
        voteService.vote(currentUser.authenticatedUser(), VoteType.UPVOTE, votableId);
    }

    @PostMapping("/downvote/{votableId}")
    public void downvote(@PathVariable UUID votableId,
                     @AuthenticationPrincipal SecurityUser currentUser) {
        voteService.vote(currentUser.authenticatedUser(), VoteType.DOWNVOTE, votableId);
    }

    @PostMapping("/unvote/{objectId}")
    public void unvote(@PathVariable UUID objectId,
                       @AuthenticationPrincipal SecurityUser currentUser) {
        voteService.unvote(currentUser.authenticatedUser(), objectId);
    }

    @GetMapping("/votevalue/{votableId}")
    public long getBookVoteValue(@PathVariable UUID votableId) {
        return voteService.getVoteValue(votableId);
    }

    @PostMapping("/hasvoted/{votableId}")
    public HasVoteDTO hasVoted(@PathVariable UUID votableId,
                              @AuthenticationPrincipal SecurityUser currentUser) {
        var user = currentUser.authenticatedUser();
        return voteService.hasVoted(user, votableId);
    }

}