package fr.uge.booqin.infra.security.auth;

import fr.uge.booqin.domain.model.Admin;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.persistence.entity.user.AuthIdentity;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.user.AuthIdentityRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

/**
 * Service that loads user data from the database and maps it to a {@link SecurityUser} object.
 * A service dedicated to the authentication process.
 */
public class UserIdentityService implements UserDetailsService {

    private final AuthIdentityRepository authIdentityRepository;
    private final UserRepository userRepository;

    public UserIdentityService(AuthIdentityRepository authIdentityRepository, UserRepository userRepository) {
        this.authIdentityRepository = authIdentityRepository;
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<AuthIdentity> authIdentity;
        if(usernameOrEmail.equals("booqin")) {
            throw new UsernameNotFoundException(usernameOrEmail);
        }
        if(usernameOrEmail.contains("@")) {
            authIdentity = authIdentityRepository.findByEmail(usernameOrEmail);
        } else {
            authIdentity = authIdentityRepository.findByUsername(usernameOrEmail);
        }
        var identity = authIdentity.orElseThrow(() -> new UsernameNotFoundException("Auth Identity not found for " + usernameOrEmail));
        return userRepository.findByAuthIdentityId(identity.getId())
                .map(userEntity -> createSecurityUser(userEntity, identity))
                .orElseThrow(() -> new UsernameNotFoundException("User `" + usernameOrEmail + "` not found"));
    }

    public UserDetails loadUserById(UUID userId) {
        var identity = authIdentityRepository.findByUserId(userId)
                .orElseThrow(() -> new UsernameNotFoundException("Auth Identity not found for " + userId));
        return userRepository.findById(userId)
                .map(userEntity -> createSecurityUser(userEntity, identity))
                .orElseThrow(() -> new UsernameNotFoundException("User `" + userId + "` not found"));
    }

    private static SecurityUser createSecurityUser(UserEntity userEntity, AuthIdentity identity) {
        var user = from(userEntity);
        return new SecurityUser(user.username(),
                identity.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(Role.from(user).toString())),
                user);
    }

    private static User from(UserEntity userEntity) {
        var user = User.of(userEntity.getId(),
                userEntity.getUsername(),
                userEntity.getEmail(),
                userEntity.getFollowable().getId());
        return userEntity.getIsAdmin() ? Admin.of(user) : user;
    }

    public enum Role {
        USER, ADMIN;

        static Role from(User user) {
            return user instanceof Admin ? ADMIN : USER;
        }

    }
}
