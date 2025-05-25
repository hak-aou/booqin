package fr.uge.booqin.infra.persistence.fixtures.comment;

import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.comment.CommentService;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class CommentFixtures {
    private final CommentService commentService;
    private final BookCommentGenerator bookCommentGenerator;

    public CommentFixtures(CommentService commentService,
                           BookCommentGenerator bookCommentGenerator) {
          this.commentService = commentService;
          this.bookCommentGenerator = bookCommentGenerator;
    }

    public Long randomComment(UserEntity author, UUID commentableId) {
        var content = bookCommentGenerator.generateComment();
        return commentService.comment(UserMapper.from(author), commentableId, content).id();
    }

    public Long replyRandomComment(UserEntity author, Long commentId) {
        var content = bookCommentGenerator.generateComment();
        return commentService.replyComment(UserMapper.from(author), commentId, content).id();
    }
}
