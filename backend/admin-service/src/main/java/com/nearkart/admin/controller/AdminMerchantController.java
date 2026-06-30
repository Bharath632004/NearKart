package com.nearkart.admin.controller;

import com.nearkart.admin.client.MerchantServiceClient;
import com.nearkart.admin.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/merchants")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminMerchantController {

    private final AuditLogService auditLogService;
    private final MerchantServiceClient merchantServiceClient;

    @PutMapping("/{merchantId}/approve")
    public ResponseEntity<String> approveMerchant(
            @PathVariable Long merchantId,
            @AuthenticationPrincipal UserDetails admin) {
        merchantServiceClient.approveMerchant(merchantId);
        auditLogService.log(admin.getUsername(), "APPROVE_MERCHANT", "MERCHANT", merchantId, null);
        return ResponseEntity.ok("Merchant " + merchantId + " approved");
    }

    @PutMapping("/{merchantId}/reject")
    public ResponseEntity<String> rejectMerchant(
            @PathVariable Long merchantId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails admin) {
        merchantServiceClient.rejectMerchant(merchantId, reason);
        auditLogService.log(admin.getUsername(), "REJECT_MERCHANT", "MERCHANT", merchantId, "Reason: " + reason);
        return ResponseEntity.ok("Merchant " + merchantId + " rejected");
    }

    @PutMapping("/{merchantId}/suspend")
    public ResponseEntity<String> suspendMerchant(
            @PathVariable Long merchantId,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails admin) {
        merchantServiceClient.suspendMerchant(merchantId, reason);
        auditLogService.log(admin.getUsername(), "SUSPEND_MERCHANT", "MERCHANT", merchantId, "Reason: " + reason);
        return ResponseEntity.ok("Merchant " + merchantId + " suspended");
    }
}
