package com.nearkart.discovery.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.context.annotation.Configuration;

/**
 * Custom Eureka server event listener.
 * Logs useful startup information and registered services.
 */
@Configuration
public class EurekaStartupLogger {

    private static final Logger logger =
            LoggerFactory.getLogger(EurekaStartupLogger.class);

    @Value("${server.port:8761}")
    private int port;

    @Value("${spring.application.name:discovery-server}")
    private String appName;

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        logger.info("╔══════════════════════════════════════════════╗");
        logger.info("║   NearKart Discovery Server Started          ║");
        logger.info("╠══════════════════════════════════════════════╣");
        logger.info("║  Service  : {}                               ║", appName);
        logger.info("║  Port     : {}                               ║", port);
        logger.info("║  Dashboard: http://localhost:{}/             ║", port);
        logger.info("║  Eureka   : http://localhost:{}/eureka/      ║", port);
        logger.info("╚══════════════════════════════════════════════╝");
    }
}
