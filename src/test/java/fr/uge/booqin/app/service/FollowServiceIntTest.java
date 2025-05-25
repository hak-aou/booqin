package fr.uge.booqin.app.service;


import fr.uge.booqin.app.dto.follow.FollowingsRequest;
import fr.uge.booqin.app.dto.pagination.PageRequest;
import fr.uge.booqin.app.dto.pagination.PaginatedResult;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.app.service.follow.FollowService;
import fr.uge.booqin.app.service.notification.NotificationService;
import fr.uge.booqin.domain.model.notification.FollowNotification;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.entity.follow.FollowEntity;
import fr.uge.booqin.infra.persistence.fixtures.UserFixtures;
import fr.uge.booqin.infra.persistence.repository.follow.FollowRepository;
import fr.uge.booqin.infra.persistence.repository.follow.FollowableRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;
import fr.uge.booqin.app.mapper.UserMapper;

import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(
        locations = "/application-test.properties",
        properties = {
                "spring.profiles.active=test"
        }
)
@Transactional
class FollowServiceIntTest {

    @Autowired
    private FollowService followService;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private UserFixtures userFixtures;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private FollowableRepository followableRepository;

    @Test
    @Rollback
    void followSuccess() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var follower = users.get(0);
        var followee = users.get(1);
        // when
        followService.follow(UserMapper.from(follower), followee.getFollowable().getId());
        // then
        assertFalse(followRepository.findAll().isEmpty());

        assertEquals(1, followee.getFollowable().getFollowers().size());
        var follows = followee.getFollowable().getFollowers();
        assertEquals(1, follows.size());
        assertEquals(follower.getId(),
                follows
                        .stream()
                        .findFirst()
                        .map(FollowEntity::getUser)
                        .map(UserEntity::getId)
                        .orElse(null)
        );
    }

    @Test
    @Rollback
    void followableNotFoundThrowsException() {
        // given
        var users = userFixtures.createDummyUsers(1);
        var follower = UserMapper.from(users.getFirst());
        // then
        assertThrows(TheirFaultException.class,
                // when
                () -> followService.follow(follower, UUID.randomUUID()));
    }

    @Test
    @Rollback
    void followableAlreadyFollowedThrowsException() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var follower = UserMapper.from(users.get(0));
        var followee = users.get(1);
        followService.follow(follower, followee.getFollowable().getId());
        followableRepository.flush();
        assertThrows(DataAccessException.class,
                 // when
                () -> {
                    followService.follow(follower, followee.getFollowable().getId());
                    followableRepository.flush();
                });
    }

    @Test
    @Rollback
    void findRelationShipBetweenUserAndFollowable() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var follower = UserMapper.from(users.get(0));
        var followee = users.get(1);
        followService.follow(follower, followee.getFollowable().getId());
        // when
        var relationship = followService.relationshipWithFollowable(follower, followee.getFollowable().getId());
        // then
        assertTrue(relationship.following());
    }

    @Test
    @Rollback
    void unfollowSuccess() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var follower = UserMapper.from(users.get(0));
        var followee = users.get(1);
        followService.follow(follower, followee.getFollowable().getId());
        // when
        followService.unfollow(follower, followee.getFollowable().getId());
        // then
        assertTrue(followRepository.findAll().isEmpty());
        assertEquals(0, followee.getFollowable().getFollowers().size());
    }

    @Test
    @Rollback
    void unfollowNotFoundThrowsException() {
        // given
        var users = userFixtures.createDummyUsers(1);
        var follower = UserMapper.from(users.getFirst());
        // then
        assertThrows(TheirFaultException.class,
                // when
                () -> followService.unfollow(follower, UUID.randomUUID()));
    }

    @Test
    @Rollback
    void unfollowNotFollowedThrowsException() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var follower = UserMapper.from(users.get(0));
        var followee = users.get(1);
        // then
        assertDoesNotThrow(
                // when
                () -> followService.unfollow(follower, followee.getFollowable().getId()) // idempotent returns nothing
        );
    }

    @Test
    @Rollback
    void findFollowings() {
        // given
        var users = userFixtures.createDummyUsers(3);
        var follower = UserMapper.from(users.get(0));
        var followee = users.get(1);
        var followee2 = users.get(2);
        followService.follow(follower, followee.getFollowable().getId());
        followService.follow(follower, followee2.getFollowable().getId());
        // when
        PaginatedResult<PublicProfileDTO> followings = followService.followingsOfUser(
                new FollowingsRequest(follower.id(), new PageRequest(0, 10)
                ));
        // then
        assertEquals(2, followings.data().size());
        assertThat(followings.data().stream().map(PublicProfileDTO::id).toList(),
                containsInAnyOrder(followee.getId(), followee2.getId())
        );
    }

    @Test
    @Rollback
    void followUserSuccessCreateNotification() {
        // given
        var users = userFixtures.createDummyUsers(2);
        var follower = users.get(0);
        var followee = users.get(1);
        // when
        followService.followUser(UserMapper.from(follower), followee.getFollowable().getId());
        // then
        var notifications = notificationService.getUserNotifications(UserMapper.from(followee),
                new PageRequest(0, 10));
        assertEquals(1, notifications.notifications().numberInPage());
        assertEquals(notifications.notifications().data().size(), notifications.notifications().numberInPage());
        assertFalse(notifications.notifications().data().getFirst().notification().base().read());
        var followNotification = (FollowNotification) notifications.notifications().data().getFirst().notification();
        assertEquals(followNotification.username(), follower.getUsername());
    }

}
