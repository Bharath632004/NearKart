package com.nearkart.gateway.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Circuit-breaker fallback endpoint.
 * All failing routes forward here via: fallbackUri: forward:/fallback
 */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    private static final Logger log = LoggerFactory.getLogger(FallbackController.class);

    @GetMapping
    public Mono<ResponseEntity<Map<String, Object>>> fallback(ServerWebExchange exchange) {
        String path = exchange.getRequest().getURI().getPath();
        log.warn("Circuit breaker triggered for path: {}", path);

        Map<String, Object> body = Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status",    HttpStatus.SERVICE_UNAVAILABLE.value(),
                "error",     "Service Unavailable",
                "message",   "The downstream service is temporarily unavailable. Please try again shortly.",
                "path",      path
        );
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
