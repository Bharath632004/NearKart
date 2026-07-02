package com.nearkart.merchant.config;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT Auth Filter.
 * In the microservice architecture, the API Gateway already validates the JWT
 * and forwards the user identity via X-User-Id and X-User-Role headers.
 *
 * SECURITY NOTE: The API Gateway MUST strip X-User-Id and X-User-Role headers
 * from all incoming external requests before adding its own trusted values.
 * An optional internal gateway secret (X-Gateway-Secret) is validated here
 * when configured, preventing header-injection attacks on direct service access.
 *
 * This filter supports BOTH modes:
 *   1. Trusting gateway-injected headers (preferred in production — set GATEWAY_SECRET).
 *   2. Parsing the Bearer token directly (useful for direct service calls / dev).
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Value("${gateway.secret:}")
    private String gatewaySecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String userId = request.getHeader("X-User-Id");
            String userRole = request.getHeader("X-User-Role");

            if (StringUtils.hasText(userId)) {
                // Validate gateway secret when configured
                if (StringUtils.hasText(gatewaySecret)) {
                    String incomingSecret = request.getHeader("X-Gateway-Secret");
                    if (!gatewaySecret.equals(incomingSecret)) {
                        log.warn("Rejected request: invalid or missing X-Gateway-Secret from {}",
                                request.getRemoteAddr());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                                "Missing or invalid gateway secret");
                        return;
                    }
                }
                String role = StringUtils.hasText(userRole) ? userRole : "ROLE_MERCHANT";
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId,
                                null,
                                List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                // Mode 2: Parse Bearer token directly
                String token = extractBearerToken(request);
                if (StringUtils.hasText(token) && jwtUtil.isTokenValid(token)) {
                    Claims claims = jwtUtil.extractAllClaims(token);
                    String subject = claims.getSubject();
                    String role = claims.get("role", String.class);
                    if (role == null) role = "ROLE_MERCHANT";

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(
                                    subject,
                                    null,
                                    List.of(new SimpleGrantedAuthority(role)));
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String extractBearerToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
