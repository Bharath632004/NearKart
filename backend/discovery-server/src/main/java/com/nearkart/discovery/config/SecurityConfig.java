package com.nearkart.discovery.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for the Eureka Dashboard.
 * Protects the Eureka UI and endpoints with HTTP Basic Auth.
 * Microservices must provide credentials in their eureka.client.service-url.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${eureka.security.username:admin}")
    private String username;

    @Value("${eureka.security.password:admin123}")
    private String password;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF so Eureka clients can register without issues
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/eureka/**")
            )
            .authorizeHttpRequests(auth -> auth
                // Allow actuator health checks without authentication
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Require authentication for Eureka dashboard and all other endpoints
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    public InMemoryUserDetailsManager userDetailsManager() {
        UserDetails admin = User.builder()
                .username(username)
                .password(passwordEncoder().encode(password))
                .roles("ADMIN")
                .build();
        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
