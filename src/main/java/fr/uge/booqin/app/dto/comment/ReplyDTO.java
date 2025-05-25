package fr.uge.booqin.app.dto.comment;

public record ReplyDTO(
        long parentId,
        String content
) {

}
