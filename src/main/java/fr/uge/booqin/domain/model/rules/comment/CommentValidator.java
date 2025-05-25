package fr.uge.booqin.domain.model.rules.comment;

import fr.uge.booqin.domain.model.exception.TheirFaultException;

@FunctionalInterface
public interface CommentValidator {
    boolean validate(String comment);

    static CommentValidator useDefault(int maxCommentLength) {
        return comment -> {
            if (comment == null) {
                throw new TheirFaultException("Content cannot be null");
            }
            if (comment.isBlank()) {
                throw new TheirFaultException("Content cannot be empty");
            }
            if (comment.length() > maxCommentLength) {
                throw new TheirFaultException("Content is too long");
            }
            return true;
        };
    }
}
