package fr.uge.booqin.app.dto.follow;

import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.RequestWithPagination;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;

import java.util.UUID;

@Validated
public record  FollowersRequest (
        UUID objectId,
        @Valid
        PageRequest pageRequest
)  implements RequestWithPagination {
}
