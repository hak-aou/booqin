package fr.uge.booqin.infra.security.auth.filter;

import fr.uge.booqin.infra.security.auth.UserIdentityService;
import fr.uge.booqin.infra.security.auth.jwt.JwtUtil;
import fr.uge.booqin.infra.security.exception.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/*
    This is a filter to have a simple custom JWT authentication without having a full OAuth2 implementation.

    Here we are defining a custom filter as proposed in spring doc:
    https://docs.spring.io/spring-security/reference/servlet/architecture.html#adding-custom-filter.

    This way we can make coexist JWT authentication and form login (session based for JTE) in the same application
 */
@Component
public class JwtBearerAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserIdentityService userIdentityService;

    public JwtBearerAuthFilter(JwtUtil jwtUtil, UserDetailsService userIdentityService) {
        this.jwtUtil = jwtUtil;
        this.userIdentityService = (UserIdentityService) userIdentityService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain chain)
            throws ServletException, IOException {
        // Get JWT from Authorization header
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }
        // get access_token (must remove "Bearer " prefix)
        var token = authHeader.substring(7);
        UUID userId;
        try {
            userId = UUID.fromString(jwtUtil.validateAndGetSubject(token));
        } catch (AuthenticationException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails securityUser;
            try {
                securityUser = userIdentityService.loadUserById(userId);
                var authToken = new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                // Sets the authenticated user in Spring Security's context
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } catch (UsernameNotFoundException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        chain.doFilter(request, response);
    }
}