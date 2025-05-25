package fr.uge.booqin.app.dto.comment;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

import java.util.List;

public record CommentTreeDTO(
        @JsonUnwrapped
        ShallowCommentDTO commentData,
        List<CommentTreeDTO> replies
) {
}
