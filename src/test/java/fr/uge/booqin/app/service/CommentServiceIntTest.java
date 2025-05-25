package fr.uge.booqin.app.service;

import fr.uge.booqin.app.dto.comment.CommentTreeDTO;
import fr.uge.booqin.app.dto.comment.ShallowCommentDTO;
import fr.uge.booqin.app.dto.comment.CommentRequest;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.comment.CommentService;
import fr.uge.booqin.domain.model.Admin;
import fr.uge.booqin.infra.persistence.entity.comment.CommentEntity;
import fr.uge.booqin.infra.persistence.fixtures.BookFixtures;
import fr.uge.booqin.infra.persistence.fixtures.UserFixtures;
import fr.uge.booqin.infra.persistence.fixtures.comment.BookCommentGenerator;
import fr.uge.booqin.infra.persistence.repository.comment.CommentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        locations = "/application-test.properties",
        properties = {
                "spring.profiles.active=test"
        }
)
@Transactional
public class CommentServiceIntTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserFixtures userFixtures;

    @Autowired
    private BookFixtures bookFixtures;

    @Autowired
    private CommentRepository commentRepository;

    private final BookCommentGenerator bookCommentGenerator = new BookCommentGenerator();

    @Test
    @Rollback
    void commentOnCommentable() {
        // given
        var users = userFixtures.createDummyUsers(1);
        var commenter = users.getFirst();
        var commentable = bookFixtures.randomBook();
        var message = bookCommentGenerator.generateComment();
        // when
        commentService.comment(UserMapper.from(commenter), commentable.getCommentable().getId(), message);
        // then
        assertEquals(1, commentable.getCommentable().getComments().size());
        assertEquals(message, commentable.getCommentable().getComments().getFirst().getContent());
    }

    @Test
    @Rollback
    void multipleCommentsFromSameUserOnCommentable() {
        // given
        var users = userFixtures.createDummyUsers(1);
        var commenter = users.getFirst();
        var commentable = bookFixtures.randomBook();
        var messages = bookCommentGenerator.generateComments(5);
        // when
        for (var message : messages) {
            commentService.comment(UserMapper.from(commenter), commentable.getCommentable().getId(), message);
        }
        // then
        var commentsContent = commentable.getCommentable().getComments().stream().map(CommentEntity::getContent).toArray();
        assertThat(messages,containsInAnyOrder(commentsContent));
    }

    @Test
    @Rollback
    void oneCommentByMultipleUsersOnCommentable() {
        // given
        var numberOfCommenters = 5;
        var commenters = userFixtures.createDummyUsers(numberOfCommenters);
        var commentable = bookFixtures.randomBook();
        var messages = bookCommentGenerator.generateComments(numberOfCommenters);
        // when
        for (int i = 0; i < numberOfCommenters; i++) {
            commentService.comment(UserMapper.from(commenters.get(i)), commentable.getCommentable().getId(), messages.get(i));
        }

        // then
        var comments = commentable.getCommentable().getComments();
        var commentsContent = comments.stream().map(CommentEntity::getContent).toArray();
        assertThat(messages,containsInAnyOrder(commentsContent));
    }

    @Test
    @Rollback
    void replyToAComment() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var message = bookCommentGenerator.generateComment();
        var comment = new CommentEntity();
        comment.setAuthor(user);
        comment.setCommentable(commentable.getCommentable());
        comment.setContent(message);
        comment.setCreatedAt(Instant.now());
        comment = commentRepository.save(comment);
        commentRepository.flush();
        System.out.println(Arrays.toString(commentRepository.findAll().stream().map(CommentEntity::getId).toArray()));
        var replyMessage = bookCommentGenerator.generateComment();
        // when
        commentService.replyComment(UserMapper.from(user), comment.getId(), replyMessage);
        // then
        commentRepository
                .findById(comment.getId())
                .ifPresentOrElse(
                        c -> {
                            var replies = c.getReplies();
                            assertEquals(1, replies.size());
                            assertEquals(replyMessage, replies.getFirst().getContent());
                        },
                        () -> fail("Comment not found")
                );
    }

    @Test
    @Rollback
    void editCommentOnNonExistingCommentThrowsException() {
        // check that the comment does not exist
        assertTrue(commentRepository.findAll().isEmpty());
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var message = bookCommentGenerator.generateComment();
        // then
        assertThrows(TheirFaultException.class,
                // when
                () -> commentService.editComment(UserMapper.from(user), 0L, message));
    }

    @Test
    @Rollback
    void editComment() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var message = "This is a comment";
        var comment = new CommentEntity();
        comment.setAuthor(user);
        comment.setCommentable(commentable.getCommentable());
        comment.setContent(message);
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        var newMessage = bookCommentGenerator.generateComment();
        // when
        commentService.editComment(UserMapper.from(user), comment.getId(), newMessage);
        // then
        commentRepository.findById(comment.getId()).ifPresentOrElse(
                c -> assertEquals(newMessage, c.getContent()),
                () -> fail("Comment not found")
        );
    }

    @Test
    @Rollback
    void obfuscateCommentOnNonExistingCommentThrowsException() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        // then
        assertThrows(TheirFaultException.class,
                // when
                () -> commentService.obfuscateComment(UserMapper.from(user), 0L));
    }

    @Test
    @Rollback
    void obfuscateCommentNoReplies() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var message = "This is a comment";
        var comment = new CommentEntity();
        comment.setAuthor(user);
        comment.setCommentable(commentable.getCommentable());
        comment.setContent(message);
        comment.setCreatedAt(Instant.now());
        commentRepository.save(comment);
        // when
        commentService.obfuscateComment(UserMapper.from(user), comment.getId());
        // then
        assertTrue(commentRepository.findById(comment.getId()).isEmpty());
    }

    @Test
    @Rollback
    void obfuscateCommentWithReplies() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var message = "This is a comment";
        commentService.comment(UserMapper.from(user), commentable.getCommentable().getId(), message);
        var commentId = commentable.getCommentable().getComments().getFirst().getId();
        var replyMessage = "This is a reply";
        commentService.replyComment(UserMapper.from(user), commentId, replyMessage);
        // when
        commentService.obfuscateComment(UserMapper.from(user), commentId);
        // then
        // we want to keep the replies so the parent is just wiped of content and author
        assertFalse(commentRepository.findById(commentId).isEmpty());
        assertNull(commentRepository.findById(commentId).get().getContent());
        assertNull(commentRepository.findById(commentId).get().getAuthor());
    }

    @Test
    @Rollback
    void removeCommentAsUserFailure() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var message = "This is a comment";
        commentService.comment(UserMapper.from(user), commentable.getCommentable().getId(), message);
        var commentId = commentable.getCommentable().getComments().getFirst().getId();
        var replyMessage = "This is a reply";
        commentService.replyComment(UserMapper.from(user), commentId, replyMessage);
        // then
        assertThrows(TheirFaultException.class,
                // when
                () -> commentService.deleteComment(UserMapper.from(user), commentId));
    }

    @Test
    @Rollback
    void removeCommentAsAdmin() {
        // given
        var userEntity = userFixtures.createDummyUsers(1).getFirst();
        var admin = Admin.of(UserMapper.from(userEntity));
        var commentable = bookFixtures.randomBook();
        var message = "This is a comment";
        commentService.comment(admin, commentable.getCommentable().getId(), message);
        var commentId = commentable.getCommentable().getComments().getFirst().getId();
        var replyMessage = "This is a reply";
        commentService.replyComment(admin, commentId, replyMessage);
        // when
        commentService.deleteComment(admin, commentId);
        // then
        assertTrue(commentRepository.findById(commentId).isEmpty());
    }

    @Test
    @Rollback
    void findCommentsOfUser() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var messages = bookCommentGenerator.generateComments(5);
        for (var message : messages) {
            commentService.comment(UserMapper.from(user), commentable.getCommentable().getId(), message);
        }
        // when
        var paginatedComments = commentService.findComments(
                new CommentRequest<>(commentable.getCommentable().getId(), new PageRequest(0, 5))
        );
        // then
        assertEquals(5, paginatedComments.data().size());
        var commentsContent = paginatedComments.data().stream().map(CommentTreeDTO::commentData).map(ShallowCommentDTO::content).toArray();
        assertThat(messages, containsInAnyOrder(commentsContent));
    }

    @Test
    @Rollback
    void findRepliesOfComment() {
        // given
        var user = userFixtures.createDummyUsers(1).getFirst();
        var commentable = bookFixtures.randomBook();
        var message = "This is a comment";
        var comment = new CommentEntity();
        comment.setAuthor(user);
        comment.setCommentable(commentable.getCommentable());
        comment.setContent(message);
        comment.setCreatedAt(Instant.now());
        comment = commentRepository.save(comment);
        var replyMessage = "This is a reply";
        commentService.replyComment(UserMapper.from(user), comment.getId(), replyMessage);
        // when
        var paginatedReplies = commentService.repliesOfComment(
                new CommentRequest<>(comment.getId(), new PageRequest(0, 5))
        );
        // then
        assertEquals(1, paginatedReplies.data().size());
        assertEquals(replyMessage, paginatedReplies.data().getFirst().content());
    }

}
