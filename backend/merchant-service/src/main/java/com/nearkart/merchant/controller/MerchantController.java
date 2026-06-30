package com.nearkart.merchant.controller;

import com.nearkart.merchant.dto.*;
import com.nearkart.merchant.entity.MerchantStatus;
import com.nearkart.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/merchants")
@RequiredArgsConstructor
@Tag(name = "Merchant", description = "Merchant registration and profile APIs")
public class MerchantController {

    private final MerchantService merchantService;

    @Operation(summary = "Register a new merchant")
    @PostMapping
    public ResponseEntity<MerchantResponse> register(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody MerchantRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.registerMerchant(userId, request));
    }

    @Operation(summary = "Get current merchant profile")
    @GetMapping("/me")
    public ResponseEntity<MerchantResponse> getProfile(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(merchantService.getMerchantByUserId(userId));
    }

    @Operation(summary = "Update current merchant profile")
    @PatchMapping("/me")
    public ResponseEntity<MerchantResponse> updateProfile(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody MerchantUpdateRequest request) {
        return ResponseEntity.ok(merchantService.updateMerchantProfile(userId, request));
    }

    @Operation(summary = "Get merchant by ID (admin/internal)")
    @GetMapping("/{merchantId}")
    public ResponseEntity<MerchantResponse> getById(@PathVariable UUID merchantId) {
        return ResponseEntity.ok(merchantService.getMerchantById(merchantId));
    }

    @Operation(summary = "Update merchant status (admin)")
    @PatchMapping("/{merchantId}/status")
    public ResponseEntity<MerchantResponse> updateStatus(
            @PathVariable UUID merchantId,
            @RequestParam MerchantStatus status) {
        return ResponseEntity.ok(merchantService.updateMerchantStatus(merchantId, status));
    }

    // ─── KYC ────────────────────────────────────────────────────────────────────

    @Operation(summary = "Upload KYC document")
    @PostMapping("/kyc")
    public ResponseEntity<KycDocumentResponse> uploadKyc(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("documentType") String documentType) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.uploadKycDocument(userId, file, documentType));
    }

    @Operation(summary = "Get all KYC documents")
    @GetMapping("/kyc")
    public ResponseEntity<List<KycDocumentResponse>> getKycDocuments(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(merchantService.getKycDocuments(userId));
    }

    @Operation(summary = "Verify or reject KYC document (admin)")
    @PatchMapping("/kyc/{documentId}/verify")
    public ResponseEntity<KycDocumentResponse> verifyKyc(
            @PathVariable UUID documentId,
            @RequestParam boolean approved,
            @RequestParam(required = false) String rejectionReason) {
        return ResponseEntity.ok(merchantService.verifyKycDocument(documentId, approved, rejectionReason));
    }

    // ─── Analytics ──────────────────────────────────────────────────────────────

    @Operation(summary = "Get merchant analytics summary")
    @GetMapping("/analytics")
    public ResponseEntity<AnalyticsSummaryResponse> getAnalytics(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(merchantService.getMerchantAnalytics(userId));
    }

    // ─── Settlements ─────────────────────────────────────────────────────────────

    @Operation(summary = "Get settlement history")
    @GetMapping("/settlements")
    public ResponseEntity<List<SettlementResponse>> getSettlements(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(merchantService.getMerchantSettlements(userId));
    }
}
