package com.nearkart.inventoryservice.security;

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
import java.util.stream.Collectors;

/**
 * JWT Auth Filter.
 * Supports two authentication modes:
 *  1. Gateway-header mode  — trusts X-User-Id / X-User-Role injected by the API Gateway.
 *     When GATEWAY_SECRET is configured the X-Gateway-Secret header is validated to
 *     prevent direct header-injection attacks on internal service ports.
 *  2. Bearer-token mode    — parses the Authorization: Bearer <jwt> header directly
 *     (useful for developer / direct-call scenarios).
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
                // Mode 1: gateway-header path
                if (StringUtils.hasText(gatewaySecret)) {
                    String incoming = request.getHeader("X-Gateway-Secret");
                    if (!gatewaySecret.equals(incoming)) {
                        log.warn("Rejected request: invalid X-Gateway-Secret from {}", request.getRemoteAddr());
                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid gateway secret");
                        return;
                    }
                }
                // Roles from gateway are already prefixed (e.g. ROLE_MERCHANT) or bare — normalise once
                String role = StringUtils.hasText(userRole) ? normaliseRole(userRole) : "ROLE_MERCHANT";
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(
                                userId, null, List.of(new SimpleGrantedAuthority(role)));
                SecurityContextHolder.getContext().setAuthentication(auth);

            } else {
                // Mode 2: Bearer token path
                String header = request.getHeader("Authorization");
                if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
                    String token = header.substring(7);
                    if (jwtUtil.validateToken(token)) {
                        String username = jwtUtil.extractUsername(token);
                        List<String> roles = jwtUtil.extractRoles(token);
                        List<SimpleGrantedAuthority> authorities = roles.stream()
                                .map(r -> new SimpleGrantedAuthority(normaliseRole(r)))
                                .collect(Collectors.toList());
                        UsernamePasswordAuthenticationToken auth =
                                new UsernamePasswordAuthenticationToken(username, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(auth);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not set user authentication: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    /** Ensures role always has exactly one ROLE_ prefix. */
    private String normaliseRole(String role) {
        return role.startsWith("ROLE_") ? role : "ROLE_" + role;
    }
}
