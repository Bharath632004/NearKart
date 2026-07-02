package com.nearkart.userservice.event;

import java.time.Instant;

public record UserRegisteredEvent(
        Long userId,
        String email,
        String name,
        Instant occurredAt
) {
    public UserRegisteredEvent(Long userId, String email, String name) {
        this(userId, email, name, Instant.now());
    }
}
