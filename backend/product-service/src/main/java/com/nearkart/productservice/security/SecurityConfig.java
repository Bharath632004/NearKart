package com.nearkart.productservice.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public read endpoints
                .requestMatchers(HttpMethod.GET,  "/api/products").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/available").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/{id}").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/search").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/filter/price").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/category/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/shop/**").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/categories").permitAll()
                .requestMatchers(HttpMethod.GET,  "/api/products/categories/**").permitAll()
                // Swagger / OpenAPI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // Actuator
                .requestMatchers("/actuator/**").permitAll()
                // All write operations require authentication
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
