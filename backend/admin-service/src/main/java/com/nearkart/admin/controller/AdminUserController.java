package com.nearkart.admin.controller;

import com.nearkart.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminUserController {

    private final AuditLogService auditLogService;

    // TODO: inject UserServiceClient (Feign) to call user-service

    @PutMapping("/{userId}/ban")
    public ResponseEntity<String> banUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails admin) {
        // userServiceClient.banUser(userId);
        auditLogService.log(admin.getUsername(), "BAN_USER", "USER", userId, null);
        return ResponseEntity.ok("User " + userId + " banned successfully");
    }

    @PutMapping("/{userId}/unban")
    public ResponseEntity<String> unbanUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails admin) {
        // userServiceClient.unbanUser(userId);
        auditLogService.log(admin.getUsername(), "UNBAN_USER", "USER", userId, null);
        return ResponseEntity.ok("User " + userId + " unbanned successfully");
    }

    @PutMapping("/{userId}/verify")
    public ResponseEntity<String> verifyUser(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails admin) {
        // userServiceClient.verifyUser(userId);
        auditLogService.log(admin.getUsername(), "VERIFY_USER", "USER", userId, null);
        return ResponseEntity.ok("User " + userId + " verified");
    }

    @PutMapping("/{userId}/role")
    public ResponseEntity<String> changeRole(
            @PathVariable Long userId,
            @RequestParam String role,
            @AuthenticationPrincipal UserDetails admin) {
        // userServiceClient.changeRole(userId, role);
        auditLogService.log(admin.getUsername(), "CHANGE_ROLE", "USER", userId, "New role: " + role);
        return ResponseEntity.ok("Role updated to " + role + " for user " + userId);
    }
}
