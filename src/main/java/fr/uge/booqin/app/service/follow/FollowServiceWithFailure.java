package fr.uge.booqin.app.service.follow;

import fr.uge.booqin.app.dto.follow.FollowRelationshipDTO;
import fr.uge.booqin.app.dto.follow.FollowersRequest;
import fr.uge.booqin.app.dto.follow.FollowingsRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.ServiceUtils;
import fr.uge.booqin.app.service.UserService;
import fr.uge.booqin.app.service.observer.obs_interface.FollowObserver;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.persistence.entity.follow.FollowEntity;
import fr.uge.booqin.infra.persistence.repository.follow.FollowableRepository;
import fr.uge.booqin.infra.persistence.repository.follow.FollowRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import jakarta.validation.Validator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class FollowServiceWithFailure {

    private final UserRepository userRepository;
    private final FollowableRepository followableRepository;
    private final FollowRepository followRepository;
    private final Validator validator;
    private final List<FollowObserver> observers;

    public FollowServiceWithFailure(
            UserRepository userRepository,
            FollowableRepository followableRepository,
            FollowRepository followRepository,
            List<FollowObserver> observers,
            Validator validator
    ) {
        this.userRepository = userRepository;
        this.followableRepository = followableRepository;
        this.followRepository = followRepository;
        this.validator = validator;
        this.observers = new ArrayList<>(observers);
    }

    /// Special case for notifying the user that they have been followed
    @Transactional
    public void followUser(User user, UUID followableIdOfUser) {
        followImpl(user, followableIdOfUser);

        var userToFollow = userRepository.findByFollowableId(followableIdOfUser)
                .map(UserMapper::from)
                .orElseThrow(() -> new TheirFaultException("User to follow not found"));

        observers.forEach(observer ->
                observer.notifyFollow(user, userToFollow));
    }

    @Transactional
    public void follow(User user, UUID followableId) {
        followImpl(user, followableId);
    }

    private void followImpl(User user, UUID followableId) {
        var followable = followableRepository.findById(followableId)
                .orElseThrow(() -> new TheirFaultException("Followable not found"));
        var userEntity = userRepository.findById(user.id())
                .orElseThrow(() -> new TheirFaultException("User not found"));
        var follow = new FollowEntity();
        follow.setUser(userEntity);
        follow.setFollowedAt(Instant.now());
        follow.setFollowing(followable);
        followable = followable.addFollower(follow);
        followableRepository.save(followable);
    }

    @Transactional
    public void unfollow(User user, UUID followableId) {
        var followable = followableRepository.findByIdWithFollowers(followableId)
                .orElseThrow(() -> new TheirFaultException("Followable not found"));
        followRepository
                .findByUser_IdAndFollowing_Id(user.id(), followableId)
                .ifPresent(value -> followableRepository.save(followable.removeFollower(value)));
    }

    @Transactional
    public PaginatedResult<PublicProfileDTO> followersOfFollowable(FollowersRequest request) {
        return ServiceUtils.paginatedRequest(validator, request,
                page -> followRepository.findAllByFollowing_Id(request.objectId(), page)
                        .map(FollowEntity::getUser)
                        .map(UserService::from)
        );
    }

    @Transactional
    public FollowRelationshipDTO relationshipWithFollowable(User user, UUID followableId) {
        var relationship = followRepository.findByUser_IdAndFollowing_Id(user.id(), followableId);
        return relationship
                .map(FollowEntity::getFollowedAt)
                .map(at -> new FollowRelationshipDTO(true, at))
                .orElse(new FollowRelationshipDTO(false, null));
    }

    @Transactional
    public PaginatedResult<PublicProfileDTO> followingsOfUser(FollowingsRequest followingsRequest) {
        return ServiceUtils.paginatedRequest(
                validator,
                followingsRequest,
             page -> userRepository.findFollowings(followingsRequest.userId(), page)
                     .map(UserService::from)

        );
    }
}
