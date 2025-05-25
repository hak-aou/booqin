package fr.uge.booqin.app.controller.rest;

import fr.uge.booqin.app.dto.comment.*;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.service.comment.CommentService;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping({"/api/comment", "/android/comment"})
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/all")
    public PaginatedResult<CommentTreeDTO> commentsOfCommentable(@RequestBody CommentRequest<UUID> commentRequestOfCommentable) {
        return commentService.findComments(commentRequestOfCommentable);
    }

    @PostMapping("")
    public ShallowCommentDTO commentCommentable(@RequestBody CommentCommentableDTO newComment,
                                                @AuthenticationPrincipal SecurityUser currentUser) {
        return commentService.comment(currentUser.authenticatedUser(), newComment.commentableId(), newComment.content());
    }

    @PostMapping("/reply")
    public ShallowCommentDTO replyToAComment(@RequestBody ReplyDTO newComment,
                                   @AuthenticationPrincipal SecurityUser currentUser) {
        return commentService.replyComment(currentUser.authenticatedUser(), newComment.parentId(), newComment.content());
    }

    @PostMapping("/replies/{commentId}")
    public PaginatedResult<CommentTreeDTO> repliesOfAComment(@PathVariable Long commentId, @RequestBody PageRequest pageRequest) {
        return commentService.repliesOfCommentAsTree(new CommentRequest<>(commentId, pageRequest));
    }

    @PatchMapping("")
    public void editComment(@RequestBody ShallowCommentDTO comment,
                            @AuthenticationPrincipal SecurityUser currentUser) {
        commentService.editComment(currentUser.authenticatedUser(), comment.id(), comment.content());
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable Long commentId,
                              @AuthenticationPrincipal SecurityUser currentUser) {
        commentService.deleteComment(currentUser.authenticatedUser(), commentId);
    }

    @PostMapping("/obfuscate/{commentId}")
    public void deleteCommentAndReplies(@PathVariable Long commentId,
                                        @AuthenticationPrincipal SecurityUser currentUser) {
        commentService.obfuscateComment(currentUser.authenticatedUser(), commentId);
    }


}
