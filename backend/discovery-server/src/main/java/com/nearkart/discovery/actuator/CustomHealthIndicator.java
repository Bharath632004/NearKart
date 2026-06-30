package com.nearkart.discovery.actuator;

import com.netflix.eureka.EurekaServerContext;
import com.netflix.eureka.EurekaServerContextHolder;
import com.netflix.eureka.registry.PeerAwareInstanceRegistry;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

/**
 * Custom health indicator that reports:
 *  - Number of registered services
 *  - Eureka registry availability
 */
@Component("eurekaRegistry")
public class CustomHealthIndicator implements HealthIndicator {

    @Override
    public Health health() {
        try {
            EurekaServerContext serverContext = EurekaServerContextHolder.getInstance().getServerContext();
            PeerAwareInstanceRegistry registry = serverContext.getRegistry();
            int registeredApps = registry.getApplications().size();

            return Health.up()
                    .withDetail("registeredServices", registeredApps)
                    .withDetail("status", "Eureka registry is running")
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
