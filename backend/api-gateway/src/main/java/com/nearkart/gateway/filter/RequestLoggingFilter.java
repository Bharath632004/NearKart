package com.nearkart.gateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

/**
 * Global filter: logs every request/response and injects a correlation ID.
 */
@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        long startTime = Instant.now().toEpochMilli();

        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Request-Id", requestId)
                .header("X-Request-Time", String.valueOf(startTime))
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate().request(mutatedRequest).build();

        log.info("[{}] --> {} {}",
                requestId,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getURI());

        return chain.filter(mutatedExchange)
                .doFinally(signal -> {
                    ServerHttpResponse response = mutatedExchange.getResponse();
                    long duration = Instant.now().toEpochMilli() - startTime;
                    log.info("[{}] <-- {} {} | status={} | {}ms",
                            requestId,
                            exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            response.getStatusCode() != null ? response.getStatusCode().value() : "?",
                            duration);
                });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE; // runs first
    }
}
