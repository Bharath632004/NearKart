package com.nearkart.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test - verifies the Spring context loads successfully
 * with Eureka Server enabled.
 */
@SpringBootTest
@TestPropertySource(properties = {
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "spring.security.user.name=test",
    "spring.security.user.password=test"
})
class DiscoveryServerApplicationTest {

    @Test
    void contextLoads() {
        // Verifies application context starts without errors
    }
}
