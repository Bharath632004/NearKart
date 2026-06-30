package com.nearkart.gateway.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Simple health/info endpoint for the gateway itself.
 */
@RestController
@RequestMapping("/gateway")
public class GatewayInfoController {

    @Value("${spring.application.name}")
    private String appName;

    @GetMapping("/info")
    public Mono<Map<String, Object>> info() {
        return Mono.just(Map.of(
                "service",   appName,
                "status",    "UP",
                "timestamp", LocalDateTime.now().toString(),
                "version",   "1.0.0",
                "routes",    new String[]{"user-service","product-service","order-service","shop-service"}
        ));
    }
}
