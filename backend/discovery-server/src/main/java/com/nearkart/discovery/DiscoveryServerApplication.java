package com.nearkart.discovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * NearKart Discovery Server
 *
 * Acts as the Eureka Service Registry for all NearKart microservices.
 * All services register here and discover each other through this server.
 *
 * Dashboard: http://localhost:8761
 */
@SpringBootApplication
@EnableEurekaServer
public class DiscoveryServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscoveryServerApplication.class, args);
    }
}
