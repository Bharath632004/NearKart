package com.nearkart.discovery.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests that:
 *  1. Unauthenticated requests to dashboard are rejected (401)
 *  2. Valid credentials grant access
 *  3. /actuator/health is publicly accessible
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "eureka.client.register-with-eureka=false",
    "eureka.client.fetch-registry=false",
    "eureka.security.username=admin",
    "eureka.security.password=admin123"
})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpoint_isPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
               .andExpect(status().isOk());
    }

    @Test
    void dashboard_withoutAuth_returns401() throws Exception {
        mockMvc.perform(get("/"))
               .andExpect(status().isUnauthorized());
    }

    @Test
    void dashboard_withValidCredentials_returns200() throws Exception {
        mockMvc.perform(get("/")
               .with(httpBasic("admin", "admin123")))
               .andExpect(status().isOk());
    }
}
