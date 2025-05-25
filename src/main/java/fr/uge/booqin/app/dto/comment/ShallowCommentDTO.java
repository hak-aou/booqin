package fr.uge.booqin.app.dto.comment;

import fr.uge.booqin.app.dto.user.PublicProfileDTO;

import java.time.Instant;
import java.util.UUID;

public record ShallowCommentDTO(
        long id,
        UUID votableId,
        PublicProfileDTO author, // nullable
        String content, // nullable
        Long parentId, // nullable
        Instant createdAt,
        int repliesCount
) {
}
