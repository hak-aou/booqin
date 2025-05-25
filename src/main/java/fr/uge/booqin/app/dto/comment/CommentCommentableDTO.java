package fr.uge.booqin.app.dto.comment;

import java.util.UUID;

public record CommentCommentableDTO(
        UUID commentableId,
        String content
) {
}
