package fr.uge.booqin.app.service.comment;

import fr.uge.booqin.app.dto.comment.CommentRequest;
import fr.uge.booqin.app.dto.comment.CommentTreeDTO;
import fr.uge.booqin.app.dto.comment.ShallowCommentDTO;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CommentService {

    private final CommentServiceWithFailure commentServiceWithFailure;

    public CommentService(CommentServiceWithFailure commentServiceWithFailure) {
        this.commentServiceWithFailure = commentServiceWithFailure;
    }


    public ShallowCommentDTO comment(User user, UUID commentableId, String content) {
        return commentServiceWithFailure.comment(user, commentableId, content);
    }

    public ShallowCommentDTO replyComment(User user, Long commentId, String content) {
        return ServiceUtils.optimisticRetry(() ->
                commentServiceWithFailure.replyComment(user, commentId, content));
    }

    public void editComment(User user, Long commentId, String content) {
        ServiceUtils.optimisticRetry(() ->
                commentServiceWithFailure.editComment(user, commentId, content));
    }

    public void obfuscateComment(User user, Long commentId) {
        ServiceUtils.optimisticRetry(() ->
                commentServiceWithFailure.obfuscateComment(user, commentId));
    }

    public void deleteComment(User user, Long commentId) {
        ServiceUtils.optimisticRetry(() ->
                commentServiceWithFailure.deleteComment(user, commentId));
    }

    public PaginatedResult<CommentTreeDTO> findComments(CommentRequest<UUID> request) {
        return commentServiceWithFailure.findComments(request);
    }

    public PaginatedResult<ShallowCommentDTO> repliesOfComment(CommentRequest<Long> commentRequest) {
        return ServiceUtils.optimisticRetry(() ->
                commentServiceWithFailure.repliesOfComment(commentRequest));
    }

    public PaginatedResult<CommentTreeDTO> repliesOfCommentAsTree(CommentRequest<Long> commentRequest) {
        return commentServiceWithFailure.repliesOfCommentAsTree(commentRequest);
    }

}
