package fr.uge.booqin.infra.security;

import com.nimbusds.jose.JWSAlgorithm;
import fr.uge.booqin.infra.persistence.repository.user.AuthIdentityRepository;
import fr.uge.booqin.infra.persistence.repository.user.UserRepository;
import fr.uge.booqin.infra.security.auth.UserIdentityService;
import fr.uge.booqin.infra.security.auth.filter.JwtBearerAuthFilter;
import fr.uge.booqin.infra.security.auth.jwt.JwtSecretValidator;
import fr.uge.booqin.infra.security.auth.jwt.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.crypto.spec.SecretKeySpec;
import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for the application <br>
 *
 * Here is what's done : <br>
 * - JTE spring mvc : session based auth, with CSRF token <br>
 * - Rest api : JWT access token as Bearer token, refresh token in http-only cookies (mitigate XSS), + strict CORS <br>
 * - Android : JWT bearer token
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain jteSecurity(HttpSecurity http) throws Exception {
        return http
                .anonymous(AbstractHttpConfigurer::disable)
                .securityMatcher(request ->
                        !request.getRequestURI().startsWith("/api")
                                && !request.getRequestURI().startsWith("/spa")
                                && !request.getRequestURI().startsWith("/android")
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/logout").permitAll()
                        .anyRequest().permitAll()
                )
                // Form login for JTE
                .logout(logout -> logout
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .headers(
                        headers ->
                                headers.contentSecurityPolicy(
                                        csp ->
                                                //csp.policyDirectives("script-src 'self' 'nonce-{nonce}';img-src * 'self' data: ; font-src 'self' data:")))
                                                csp.policyDirectives("script-src 'self' 'nonce-{nonce}'; img-src 'self' data:; font-src 'self' data:")))

                .build();
    }

    /**
     * Security configuration for API (JWT-Based Authentication)
     */
    @Bean
    public SecurityFilterChain apiSecurity(HttpSecurity http, JwtBearerAuthFilter jwtAuthFilter, CorsConfigurationSource corsConfigurationSource) throws Exception {
        return http
                .anonymous(AbstractHttpConfigurer::disable)
                .securityMatcher("/api/**", "/spa/**", "/android/**")
                // Enable CORS for API
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/user/me/**").authenticated()
                        .requestMatchers("/api/user/followers/**").authenticated()
                        .requestMatchers("/api/user/follow/**").authenticated()
                        .requestMatchers("/api/user/comment").authenticated()
                        .requestMatchers("/api/follow/**", "/android/follow/**").authenticated()
                        .requestMatchers("/api/txs/**", "/android/txs/**").authenticated()
                        .requestMatchers("/api/admin/**").authenticated()
                        //.requestMatchers("/api/user/hasvote/**").authenticated()
                        //.requestMatchers("/api/user/vote/**").authenticated()
                        //.requestMatchers("/api/user/unvote/**").authenticated()
                        //.requestMatchers("/api/user/voters/**").authenticated()
                        .requestMatchers("/api/user/me").authenticated()
                        .requestMatchers("/api/collection/getAll").authenticated()
                        .anyRequest().permitAll()
                ).formLogin(AbstractHttpConfigurer::disable)
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // No session for API
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"))
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService, BCryptPasswordEncoder passwordEncoder) {
        var authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(List.of(authProvider));
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var spaCorsConfig = getCorsConfigurationForSPA();
        var androidCorsConfig = getCorsConfigurationForAndroid();
        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", spaCorsConfig);
        source.registerCorsConfiguration("/android/**", androidCorsConfig);
        return source;
    }

    private static CorsConfiguration getCorsConfigurationForSPA() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:8080",  // Spring app (MVC)
                "http://localhost:5173",  // React app in development (Vite)
                "http://localhost:8080/spa" // React app served after build by Spring
        ));
        // all methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Content-Length", "Accept","X-Requested-With", "charset"));
        configuration.setAllowCredentials(true);
        configuration.addExposedHeader("Set-Cookie");
        return configuration;
    }

    private static CorsConfiguration getCorsConfigurationForAndroid() {
        var configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("*"));
        configuration.setAllowedMethods(List.of("*"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        return configuration;
    }

    /*
        https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/user-details-service.html
     */
    @Bean
    public UserDetailsService customUserDetailsService(AuthIdentityRepository authIdentityRepository, UserRepository userRepository) {
        return new UserIdentityService(authIdentityRepository, userRepository);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Work factor
    }

    /**
     <a href="https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html">spring oauth 2 jwt</a>
     <br>
     Same secret key for both encoding {@link JwtUtil} and
     decoding {@link SecurityConfiguration#jwtDecoder(SecretKeySpec)}
     */
    @Bean
    SecretKeySpec secretKeySpec(@Value("${booqin.jwt.secret}") String secret, JwtSecretValidator jwtSecretValidator) {
        if(secret == null) {
            System.err.println("JWT_SECRET is not set! Please set the JWT_SECRET environment variable.");
            System.exit(1);
        }

        if (!jwtSecretValidator.isSecretStrong(secret)) {
            System.err.println("JWT_SECRET is not strong enough! Please use a stronger secret.");
            System.exit(1);
        } else {
            System.out.println("JWT_SECRET is strong enough.");
        }
        return new SecretKeySpec(secret.getBytes(), JWSAlgorithm.HS256.getName());
    }

    @Bean
    public JwtDecoder jwtDecoder(SecretKeySpec secretKeySpec) {
        return NimbusJwtDecoder.withSecretKey(secretKeySpec).build();
    }

    @Bean
    public JwtSecretValidator jwtSecretValidator() {
        return new JwtSecretValidator() { };
    }
}