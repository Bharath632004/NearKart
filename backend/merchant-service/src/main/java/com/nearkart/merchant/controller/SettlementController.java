package com.nearkart.merchant.controller;

import com.nearkart.merchant.dto.SettlementResponse;
import com.nearkart.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/settlements")
@RequiredArgsConstructor
@Tag(name = "Settlements", description = "Merchant settlement history and payout APIs")
public class SettlementController {

    private final MerchantService merchantService;

    @Operation(summary = "Get all settlements for the current merchant")
    @GetMapping
    public ResponseEntity<List<SettlementResponse>> getMySettlements(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(merchantService.getMerchantSettlements(userId));
    }

    @Operation(summary = "Get a specific settlement by ID")
    @GetMapping("/{settlementId}")
    public ResponseEntity<SettlementResponse> getSettlementById(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID settlementId) {
        return ResponseEntity.ok(merchantService.getSettlementById(userId, settlementId));
    }
}
