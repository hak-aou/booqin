package fr.uge.booqin.app.service;

import fr.uge.booqin.app.dto.user.PrivateProfileDTO;
import fr.uge.booqin.app.dto.user.PublicProfileDTO;
import fr.uge.booqin.app.dto.user.RegisterRequestDTO;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.domain.model.exception.OurFaultException;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.infra.external.avatar.AvatarGenerator;
import fr.uge.booqin.infra.persistence.entity.user.UserEntity;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final AvatarGenerator avatarGenerator;
    private final BooqInConfig booqInConfig;

    public UserService(UserRepository userRepository,
                       AuthService authService,
                       AvatarGenerator avatarGenerator,
                       BooqInConfig booqInConfig) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.avatarGenerator = avatarGenerator;
        this.booqInConfig = booqInConfig;
    }

    @Transactional
    public PrivateProfileDTO findMyProfile(User user) {
        return userRepository.findById(user.id())
                .map(userEntity -> {
                    var publicProfile = from(userEntity);
                    return new PrivateProfileDTO(
                            publicProfile,
                            userEntity.getEmail(),
                            userEntity.getIsAdmin()
                    );
                })
                .orElseThrow(() -> new TheirFaultException("User not found"));
    }

    @Transactional
    public PublicProfileDTO findPublicProfile(UUID userId) {
        return userRepository.findById(userId)
                .map(UserService::from)
                .orElseThrow(() -> new TheirFaultException("User not found"));
    }

    public static PublicProfileDTO from(UserEntity user) {
        return new PublicProfileDTO(
                user.getId(),
                user.getFollowable().getId(),
                user.getUsername(),
                user.getCreationDate(),
                user.getImageUrl(),
                user.getFollowable().getFollowers().size()
        );
    }

    @Transactional
    public void register(RegisterRequestDTO request) {
        var find = userRepository.findByUsername(request.username());
        if (find.isPresent()) {
            throw new TheirFaultException("Username already taken");
        }
        var findEmail = userRepository.findByEmail(request.email());
        if (findEmail.isPresent()) {
            throw new TheirFaultException("Email already taken");
        }
        if(!booqInConfig.passphraseValidator().validate(request.phrase())) {
            throw new TheirFaultException("Phrase does not meet requirements");
        }
        var passphrase = String.join("-", request.phrase());
        var identityId = authService.createIdentity(passphrase);
        if(identityId.isEmpty()) {
            throw new OurFaultException("Could not create identity");
        }
        var user = new UserEntity();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setIsAdmin(false);
        user.setCreationDate(Instant.now());
        user.setImageUrl(avatarGenerator.generateAvatar(request.username()));
        user.setAuthIdentityId(identityId.orElseThrow());
        userRepository.save(user);
    }
}
