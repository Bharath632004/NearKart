package com.nearkart.admin.controller;

import com.nearkart.admin.model.AuditLog;
import com.nearkart.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminAuditController {

    private final AuditLogService auditLogService;

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAllLogs() {
        return ResponseEntity.ok(auditLogService.getAllLogs());
    }

    @GetMapping("/admin/{username}")
    public ResponseEntity<List<AuditLog>> getLogsByAdmin(@PathVariable String username) {
        return ResponseEntity.ok(auditLogService.getLogsByAdmin(username));
    }

    @GetMapping("/{entity}/{id}")
    public ResponseEntity<List<AuditLog>> getLogsByTarget(
            @PathVariable String entity,
            @PathVariable Long id) {
        return ResponseEntity.ok(auditLogService.getLogsByTarget(entity.toUpperCase(), id));
    }
}
