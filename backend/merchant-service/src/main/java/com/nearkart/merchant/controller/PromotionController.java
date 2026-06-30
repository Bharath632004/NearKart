package com.nearkart.merchant.controller;

import com.nearkart.merchant.dto.*;
import com.nearkart.merchant.service.MerchantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shops/{shopId}/promotions")
@RequiredArgsConstructor
@Tag(name = "Promotions", description = "Shop promotions and offers management")
public class PromotionController {

    private final MerchantService merchantService;

    @Operation(summary = "Create a promotion for a shop")
    @PostMapping
    public ResponseEntity<PromotionResponse> createPromotion(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID shopId,
            @Valid @RequestBody PromotionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.createPromotion(userId, shopId, request));
    }

    @Operation(summary = "Get all promotions for a shop")
    @GetMapping
    public ResponseEntity<List<PromotionResponse>> getPromotions(@PathVariable UUID shopId) {
        return ResponseEntity.ok(merchantService.getShopPromotions(shopId));
    }

    @Operation(summary = "Get only active promotions for a shop")
    @GetMapping("/active")
    public ResponseEntity<List<PromotionResponse>> getActivePromotions(@PathVariable UUID shopId) {
        return ResponseEntity.ok(merchantService.getActiveShopPromotions(shopId));
    }

    @Operation(summary = "Deactivate a promotion")
    @DeleteMapping("/{promotionId}")
    public ResponseEntity<PromotionResponse> deactivatePromotion(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID shopId,
            @PathVariable UUID promotionId) {
        return ResponseEntity.ok(merchantService.deactivatePromotion(userId, promotionId));
    }
}
