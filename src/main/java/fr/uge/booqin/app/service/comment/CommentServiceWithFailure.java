package fr.uge.booqin.app.service.comment;

import fr.uge.booqin.app.dto.comment.CommentTreeDTO;
import fr.uge.booqin.app.dto.comment.ShallowCommentDTO;
import fr.uge.booqin.app.dto.comment.CommentRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.domain.model.Admin;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.persistence.entity.comment.CommentEntity;
import fr.uge.booqin.infra.persistence.entity.comment.CommentableEntity;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import fr.uge.booqin.infra.persistence.repository.comment.CommentRepository;
import fr.uge.booqin.infra.persistence.repository.comment.CommentableRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Validator;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
public class CommentServiceWithFailure {
    private final UserRepository userRepository;
    private final CommentableRepository commentableRepository;
    private final CommentRepository commentRepository;
    private final Validator validator;
    private final BooqInConfig config;

    public CommentServiceWithFailure(UserRepository userRepository,
                                     CommentableRepository commentableRepository,
                                     CommentRepository commentRepository,
                                     Validator validator,
                                     BooqInConfig config
                                     ) {
        this.userRepository = userRepository;
        this.commentableRepository = commentableRepository;
        this.commentRepository = commentRepository;
        this.validator = validator;
        this.config = config;
    }

    @Transactional
    public ShallowCommentDTO comment(User user, UUID commentableId, String content) {
        content = checkAndSanitize(content);
        var commentable = commentableRepository.findById(commentableId)
                .orElseThrow(() -> new TheirFaultException("Commentable not found"));
        var comment = createComment(user, commentable, content);
        comment.setCreatedAt(Instant.now());
        comment = commentRepository.save(comment);
        commentable.addComment(comment);
        commentableRepository.save(commentable);
        return toShallow(comment);
    }

    @Transactional
    public ShallowCommentDTO replyComment(User user, Long commentId, String content) {
        content = checkAndSanitize(content);
        var comment = commentRepository.findById(commentId).orElseThrow(
                () -> new TheirFaultException("Comment not found")
        );
        var reply = createComment(user, null, content);
        reply = commentRepository.save(reply);
        comment.reply(reply);
        commentRepository.save(comment);
        return toShallow(reply);
    }

    private String checkAndSanitize(String content) {
        config.commentValidator().validate(content);
        return config.commentSanitizer().sanitize(content);
    }

    private CommentEntity createComment(User user, CommentableEntity commentable, String content) {
        var userEntity = userRepository.findById(user.id())
                .orElseThrow(() -> new TheirFaultException("User not found"));
        var comment = new CommentEntity();
        comment.setAuthor(userEntity);
        comment.setCommentable(commentable);
        comment.setContent(content);
        comment.setCreatedAt(Instant.now());
        return comment;
    }

    @Transactional
    public void editComment(User user, Long commentId, String content) {
        if(content.isBlank()) {
            throw new TheirFaultException("Content cannot be empty");
        }
        var comment = commentRepository.findCommentByIdAndAuthor(commentId, user.id())
                .orElseThrow(() -> new TheirFaultException("Comment not found"));
        comment.setContent(content);
        commentRepository.save(comment);
    }

    ///
    /// Note an actual "delete", it allows to delete the content and unlink
    /// the relation between the user and the comment. Like reddit does.
    /// The comment is only deleted if it has no replies.
    ///
    @Transactional
    public void obfuscateComment(User user, Long commentId) {
        CommentEntity comment;
        if(user instanceof Admin) {
            comment = commentRepository.findById(commentId)
                    .orElseThrow(() -> new TheirFaultException("Comment not found"));
        } else {
            comment = commentRepository.findCommentByIdAndAuthor(commentId, user.id())
                    .orElseThrow(() -> new TheirFaultException("Comment not found"));
        }
        comment.setAuthor(null);
        comment.setContent(null);
        // check if the comment has no replies
        if (comment.getReplies().isEmpty()) {
            var parent = comment.getParent();
            if(parent != null) {
                // Todo: optimistic locking, use retry
                parent = parent.removeReply(comment);
                commentRepository.save(parent);
            }
            commentRepository.delete(comment);
        }else {
            commentRepository.save(comment);
        }
    }

    ///
    /// Actual remove
    /// will remove all replies and the comment itself
    /// must be called by an admin
    @Transactional
    public void deleteComment(User user, Long commentId) {
        if(!(user instanceof Admin)) {
            throw new TheirFaultException("Only admins can delete comments");
        }
        var comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new TheirFaultException("Comment not found"));
        commentRepository.delete(comment);
    }

    @Transactional
    public PaginatedResult<CommentTreeDTO> findComments(CommentRequest<UUID> request) {
        var page = ServiceUtils.getSpringPageRequest(validator, request);
        page.withSort(Sort.Direction.DESC, "createdAt");
        var paginatedComments = commentRepository.findRootCommentsWithImmediateReplies(request.objectId(), page);
        var comments = paginatedComments.map(commentEntity -> toTree(commentEntity, 3));
        var content = comments.getContent();
        return new PaginatedResult<>(
                content,
                comments.getTotalElements(),
                content.size(),
                request.pageRequest().offset(),
                request.pageRequest().limit()
        );
    }

    @Transactional
    public PaginatedResult<ShallowCommentDTO> repliesOfComment(CommentRequest<Long> commentRequest) {
        var page = ServiceUtils.getSpringPageRequest(validator, commentRequest);
        var paginatedReplies = commentRepository.findReplies(commentRequest.objectId(), page);
        var replies = paginatedReplies.map(this::toShallow);
        return new PaginatedResult<>(
                replies.getContent(),
                replies.getTotalElements(),
                replies.getSize(),
                commentRequest.pageRequest().offset(),
                commentRequest.pageRequest().limit()
        );
    }

    @Transactional
    public PaginatedResult<CommentTreeDTO> repliesOfCommentAsTree(CommentRequest<Long> commentRequest) {
        var page = ServiceUtils.getSpringPageRequest(validator, commentRequest);
        var paginatedReplies = commentRepository.findReplies(commentRequest.objectId(), page);
        var replies = paginatedReplies.map(commentEntity -> toTree(commentEntity, 3));
        return new PaginatedResult<>(
                replies.getContent(),
                replies.getTotalElements(),
                replies.getSize(),
                commentRequest.pageRequest().offset(),
                commentRequest.pageRequest().limit()
        );
    }

    private ShallowCommentDTO toShallow(CommentEntity commentEntity) {
        var author = commentEntity.getAuthor();
        var parent = commentEntity.getParent();
        return new ShallowCommentDTO(
                commentEntity.getId(),
                commentEntity.getVotable().getId(),
                author != null ? UserService.from(commentEntity.getAuthor()) : null,
                commentEntity.getContent(),
                parent != null ? parent.getId() : null,
                commentEntity.getCreatedAt(),
                commentEntity.getReplies().size()
        );
    }

    record Pair<T, U>(T first, U second) {}

    private CommentTreeDTO toTree(CommentEntity commentEntity, int depth) {
        ShallowCommentDTO commentData = toShallow(commentEntity);
        var repliesOfRoot =  new ArrayList<CommentTreeDTO>();
        if (depth > 0) {
            // Queue for comment nodes
            Queue<Pair<CommentEntity, ArrayList<CommentTreeDTO>>> queue = new LinkedList<>();
            queue.add(new Pair<>(commentEntity, repliesOfRoot));

            // Track current depth of each node
            Map<Long, Integer> nodeDepth = new HashMap<>();
            nodeDepth.put(commentEntity.getId(), 0);

            while (!queue.isEmpty()) {
                var pair = queue.poll();
                var current = pair.first();
                var currentReplies = pair.second();
                var currentDepth = nodeDepth.get(current.getId());

                if (currentDepth >= depth) continue;

                for (var reply : current.getReplies()) {
                    var replyData = toShallow(reply);
                    var childReplies = new ArrayList<CommentTreeDTO>();
                    var replyTree = new CommentTreeDTO(replyData, childReplies);

                    currentReplies.add(replyTree);

                    // queue child node
                    nodeDepth.put(reply.getId(), currentDepth + 1);
                    if (currentDepth + 1 < depth) {
                        queue.add(new Pair<>(reply, childReplies));
                    }
                }
            }
        }

        return new CommentTreeDTO(commentData, repliesOfRoot);
    }
}
