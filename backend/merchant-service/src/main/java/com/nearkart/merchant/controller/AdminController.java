package com.nearkart.merchant.controller;

import com.nearkart.merchant.dto.*;
import com.nearkart.merchant.entity.MerchantStatus;
import com.nearkart.merchant.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@Tag(name = "Admin", description = "Admin-only merchant management APIs")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "List all merchants (with optional status filter)")
    @GetMapping("/merchants")
    public ResponseEntity<List<MerchantResponse>> getAllMerchants(
            @RequestParam(required = false) MerchantStatus status) {
        return ResponseEntity.ok(adminService.getAllMerchants(status));
    }

    @Operation(summary = "Get merchant by ID")
    @GetMapping("/merchants/{merchantId}")
    public ResponseEntity<MerchantResponse> getMerchant(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(adminService.getMerchantById(merchantId));
    }

    @Operation(summary = "Approve or reject merchant KYC status")
    @PatchMapping("/merchants/{merchantId}/status")
    public ResponseEntity<MerchantResponse> updateMerchantStatus(
            @PathVariable UUID merchantId,
            @RequestParam MerchantStatus status) {
        return ResponseEntity.ok(adminService.updateMerchantStatus(merchantId, status));
    }

    @Operation(summary = "Get all KYC documents pending verification")
    @GetMapping("/kyc/pending")
    public ResponseEntity<List<KycDocumentResponse>> getPendingKycDocuments() {
        return ResponseEntity.ok(adminService.getPendingKycDocuments());
    }

    @Operation(summary = "Verify or reject a KYC document")
    @PatchMapping("/kyc/{documentId}/verify")
    public ResponseEntity<KycDocumentResponse> verifyKyc(
            @PathVariable UUID documentId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(adminService.verifyKycDocument(documentId, approved, rejectionReason));
    }

    @Operation(summary = "Get all settlements (admin view)")
    @GetMapping("/settlements")
    public ResponseEntity<List<SettlementResponse>> getAllSettlements(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getAllSettlements(status));
    }

    @Operation(summary = "Trigger settlement for a merchant")
    @PostMapping("/settlements/trigger")
    public ResponseEntity<SettlementResponse> triggerSettlement(
            @RequestBody SettlementRequest request) {
        return ResponseEntity.ok(adminService.triggerSettlement(request));
    }

    @Operation(summary = "Mark a settlement as completed with UTR number")
    @PatchMapping("/settlements/{settlementId}/complete")
    public ResponseEntity<SettlementResponse> completeSettlement(
            @PathVariable UUID settlementId,
            @RequestParam String utrNumber) {
        return ResponseEntity.ok(adminService.completeSettlement(settlementId, utrNumber));
    }

    @Operation(summary = "Get platform-wide analytics summary")
    @GetMapping("/analytics")
    public ResponseEntity<AdminAnalyticsResponse> getPlatformAnalytics() {
        return ResponseEntity.ok(adminService.getPlatformAnalytics());
    }
}
