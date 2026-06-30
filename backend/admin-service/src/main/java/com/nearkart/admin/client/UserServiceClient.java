package com.nearkart.admin.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @PutMapping("/api/users/{userId}/ban")
    void banUser(@PathVariable Long userId);

    @PutMapping("/api/users/{userId}/unban")
    void unbanUser(@PathVariable Long userId);

    @PutMapping("/api/users/{userId}/verify")
    void verifyUser(@PathVariable Long userId);

    @PutMapping("/api/users/{userId}/role")
    void changeRole(@PathVariable Long userId, @RequestParam String role);

    @GetMapping("/api/users/count")
    Long getUserCount();
}
