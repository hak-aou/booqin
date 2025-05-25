package fr.uge.booqin.app.dto.comment;

import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.RequestWithPagination;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

@Validated
public record CommentRequest<T>(
        T objectId,
        @Valid
        PageRequest pageRequest
) implements RequestWithPagination { }
