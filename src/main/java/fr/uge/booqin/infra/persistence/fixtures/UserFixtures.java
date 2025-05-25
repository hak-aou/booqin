package fr.uge.booqin.infra.persistence.fixtures;

import fr.uge.booqin.app.mapper.UserMapper;
import fr.uge.booqin.app.service.follow.FollowService;
import fr.uge.booqin.infra.external.avatar.AvatarGenerator;
import fr.uge.booqin.infra.persistence.entity.user.AuthIdentity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.user.AuthIdentityRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.github.javafaker.Faker;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

@Component
public class UserFixtures {
    private static final Logger logger = LoggerFactory.getLogger(UserFixtures.class);

    private final AuthIdentityRepository authIdentityRepository;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AvatarGenerator avatarGenerator;
    private final FollowService followService;
    private final Faker faker = new Faker();

    public UserFixtures(AuthIdentityRepository authIdentityRepository,
                        UserRepository userRepository,
                        BCryptPasswordEncoder passwordEncoder,
                        AvatarGenerator avatarGenerator,
                        FollowService followService
    ) {
        this.authIdentityRepository = authIdentityRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.avatarGenerator = avatarGenerator;
        this.followService = followService;
    }

    @Transactional
    public AuthIdentity createIdentity(String password) {
        return createAuthIdentity(password);
    }

    @Transactional
    public AuthIdentity createAdminAuthIdentity() {
        return createAuthIdentity("admin");
    }

    @Transactional
    public AuthIdentity createUserAuthIdentity() {
        return createAuthIdentity("user");
    }

    @Transactional
    public UserEntity createRandomUserWith(AuthIdentity authIdentity) {
        var username = faker.name().username();
        return createUser(
                authIdentity,
                false,
                username,
                faker.internet().emailAddress(),
                avatarGenerator.generateAvatar(username),
                Instant.now());
    }

    @Transactional
    public List<AuthIdentity> createDummyIdentities(int count) {
        var faker = new Faker();
        var dummyAccounts = new ArrayList<AuthIdentity>();
        for (int i = 0; i < count; i++) {
            var password = faker.internet().password();
            dummyAccounts.add(createAuthIdentity(password));
        }
        return dummyAccounts;
    }

    @Transactional
    public List<UserEntity> createDummyUsers(int count) {
        return createDummyIdentities(count).stream()
                .map(this::createRandomUserWith)
                .toList();
    }

    @Transactional
    public AuthIdentity createAuthIdentity(String password) {
        var userIdentity = new AuthIdentity();
        userIdentity.setPassword(passwordEncoder.encode(password));
        return authIdentityRepository.saveAndFlush(userIdentity);
    }

    @Transactional
    public UserEntity createUser(AuthIdentity identity, boolean isAdmin,
                                 String username,
                                 String email,
                                 String imageUrl,
                                 Instant creationDate) {
        var user = userRepository.findByUsername(username);
        if(user.isEmpty()) {
            var userEntity = new UserEntity();
            userEntity.setUsername(username);
            userEntity.setEmail(email);
            userEntity.setIsAdmin(isAdmin);
            userEntity.setCreationDate(creationDate);
            userEntity.setImageUrl(imageUrl);
            userEntity.setAuthIdentityId(identity.getId());
            return userRepository.save(userEntity);
        }
        //logger.debug("User {} created", user.get().getId());
        return user.orElseThrow();
    }

    @Transactional
    void makeFollow(UserEntity follower, UserEntity toFollow) {
        try {
            if(followService.relationshipWithFollowable(UserMapper.from(follower), toFollow.getFollowable().getId()).following()) {
                return;
            }
            followService.follow(UserMapper.from(follower), toFollow.getFollowable().getId());
        } catch (Exception e) {
            logger.info("Could not follow, probably a follower already");
        }
    }

}
