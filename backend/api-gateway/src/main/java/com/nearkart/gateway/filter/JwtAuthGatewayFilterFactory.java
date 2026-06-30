package com.nearkart.gateway.filter;

import com.nearkart.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Custom GatewayFilter that validates JWT from the Authorization header.
 * Use in routes as:  - JwtAuthFilter
 */
@Component
public class JwtAuthGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthGatewayFilterFactory.Config> {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthGatewayFilterFactory.class);
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            String path = request.getURI().getPath();

            // Skip auth for public paths
            if (isPublicPath(path)) {
                return chain.filter(exchange);
            }

            String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                log.warn("Missing or malformed Authorization header for path: {}", path);
                return onError(exchange, "Missing Authorization header", HttpStatus.UNAUTHORIZED);
            }

            String token = authHeader.substring(BEARER_PREFIX.length());
            if (!jwtUtil.isValid(token)) {
                log.warn("Invalid or expired JWT token for path: {}", path);
                return onError(exchange, "Invalid or expired token", HttpStatus.UNAUTHORIZED);
            }

            try {
                Claims claims = jwtUtil.validateToken(token);
                // Forward user context headers to downstream services
                ServerHttpRequest mutatedRequest = request.mutate()
                        .header("X-User-Id",   String.valueOf(jwtUtil.extractUserId(token)))
                        .header("X-User-Name", jwtUtil.extractUsername(token))
                        .header("X-User-Role", String.valueOf(jwtUtil.extractRole(token)))
                        .build();

                log.debug("JWT valid for user: {} path: {}", claims.getSubject(), path);
                return chain.filter(exchange.mutate().request(mutatedRequest).build());
            } catch (Exception e) {
                log.error("JWT processing error: {}", e.getMessage());
                return onError(exchange, "Token processing error", HttpStatus.UNAUTHORIZED);
            }
        };
    }

    private boolean isPublicPath(String path) {
        return path.startsWith("/api/auth/") ||
               path.startsWith("/actuator") ||
               path.equals("/fallback");
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                                String message,
                                HttpStatus status) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"error\":\"" + message + "\",\"status\":" + status.value() + "}";
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
        // Configurable per-route options can be added here
    }
}
