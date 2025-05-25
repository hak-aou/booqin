package fr.uge.booqin.app.service;

import fr.uge.booqin.app.dto.auth.AuthRequest;
import fr.uge.booqin.domain.model.exception.TheirFaultException;
import fr.uge.booqin.domain.model.User;
import fr.uge.booqin.domain.model.config.BooqInConfig;
import fr.uge.booqin.infra.persistence.entity.user.AuthIdentity;
import fr.uge.booqin.infra.persistence.repository.user.AuthIdentityRepository;
import fr.uge.booqin.infra.security.auth.SecurityUser;
import fr.uge.booqin.infra.security.auth.UserIdentityService;
import fr.uge.booqin.infra.security.auth.jwt.JwtData;
import fr.uge.booqin.infra.security.auth.jwt.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

@Service
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final BooqInConfig config;
    private final UserDetailsService userDetailsService;
    private final AuthIdentityRepository authIdentityRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       BooqInConfig config,
                       BCryptPasswordEncoder passwordEncoder,
                       UserDetailsService userDetailsService,
                       AuthIdentityRepository authIdentityRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.config = config;
        this.userDetailsService = userDetailsService;
        this.authIdentityRepository = authIdentityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public JwtData authenticate(AuthRequest authRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.usernameOrEmail(), authRequest.password())
        );
        var securityUser = (SecurityUser) userDetailsService.loadUserByUsername(authRequest.usernameOrEmail());
        var authenticatedUser = securityUser.authenticatedUser();
        return jwtUtil.generateTokens(
                authenticatedUser.id().toString(),
                config.authConfig().accessTokenExpiration(),
                authRequest.trustedDevice() ? config.authConfig().trustedDeviceTokenExpiration() : config.authConfig().refreshTokenExpiration()
        );
    }

    /**
     * Refresh the access token using the refresh token
     * @return the new access token
     */
    public String refreshToken(String refreshToken)  {
        Objects.requireNonNull(refreshToken, "Refresh token cannot be null");
        var subject = jwtUtil.validateAndGetSubject(refreshToken);
        if(subject == null) {
            throw new TheirFaultException("Invalid refresh token");
        }
        return jwtUtil.refreshToken(subject, config.authConfig().accessTokenExpiration());
    }

    public String quickToken(String refreshToken, Function<User, String> newSubject) {
        Objects.requireNonNull(refreshToken, "Refresh token cannot be null");
        var subject = jwtUtil.validateAndGetSubject(refreshToken);
        if(subject == null) {
            throw new TheirFaultException("Invalid refresh token");
        }
        var userDetails = (SecurityUser) ((UserIdentityService) userDetailsService).loadUserById(UUID.fromString(subject));
        return jwtUtil.generateToken(newSubject.apply(userDetails.authenticatedUser()), jwtUtil.expirationIn(Duration.ofMinutes(2)));
    }

    public String validateAndGetSubject(String token) {
        return jwtUtil.validateAndGetSubject(token);
    }

    public Optional<UUID> createIdentity(String password) {
        var identity = new AuthIdentity();
        identity.setPassword(passwordEncoder.encode(password));
        return Optional.ofNullable(authIdentityRepository.save(identity).getId());
    }
}
