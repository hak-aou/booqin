package fr.uge.booqin.infra.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class NonceFilter extends OncePerRequestFilter {

    public static final String NONCE_ATTRIBUTE = "cspNonce";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String nonce = generateNonce();
        request.setAttribute(NONCE_ATTRIBUTE, nonce);
        response.setHeader("Content-Security-Policy", "script-src 'self' 'nonce-" + nonce + "'");
        filterChain.doFilter(request, response);
    }

    private String generateNonce() {
        return UUID.randomUUID().toString();
    }
}