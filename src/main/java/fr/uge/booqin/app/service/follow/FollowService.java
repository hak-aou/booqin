package fr.uge.booqin.app.service.follow;

import fr.uge.booqin.app.dto.follow.FollowRelationshipDTO;
import fr.uge.booqin.app.dto.follow.FollowersRequest;
import fr.uge.booqin.app.dto.follow.FollowingsRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.domain.model.User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class FollowService {

    private final FollowServiceWithFailure followServiceWithFailure;
    public FollowService(FollowServiceWithFailure followServiceWithFailure
    ) {
        this.followServiceWithFailure = followServiceWithFailure;
    }

    public void followUser(User user, UUID followableIdOfUser) {
        ServiceUtils.optimisticRetry(() -> followServiceWithFailure.followUser(user, followableIdOfUser));
    }

    public void follow(User user, UUID followableId) {
        ServiceUtils.optimisticRetry(() ->
                followServiceWithFailure.follow(user, followableId));
    }

    public void unfollow(User user, UUID followableId) {
        ServiceUtils.optimisticRetry(() -> followServiceWithFailure.unfollow(user, followableId));
    }

    public PaginatedResult<PublicProfileDTO> followersOfFollowable(FollowersRequest request) {
        return followServiceWithFailure.followersOfFollowable(request);
    }

    public FollowRelationshipDTO relationshipWithFollowable(User user, UUID followableId) {
        return followServiceWithFailure.relationshipWithFollowable(user, followableId);
    }

    public PaginatedResult<PublicProfileDTO> followingsOfUser(FollowingsRequest followingsRequest) {
        return followServiceWithFailure.followingsOfUser(followingsRequest);
    }
}
