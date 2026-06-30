package com.nearkart.merchant.controller;

import com.nearkart.merchant.dto.*;
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
@RequestMapping("/api/v1/shops")
@RequiredArgsConstructor
@Tag(name = "Shop", description = "Shop management and discovery APIs")
public class ShopController {

    private final MerchantService merchantService;

    @Operation(summary = "Create a new shop")
    @PostMapping
    public ResponseEntity<ShopResponse> createShop(
            @RequestHeader("X-User-Id") UUID userId,
            @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(merchantService.createShop(userId, request));
    }

    @Operation(summary = "Get all shops of the current merchant")
    @GetMapping("/my")
    public ResponseEntity<List<ShopResponse>> getMyShops(
            @RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(merchantService.getMerchantShops(userId));
    }

    @Operation(summary = "Get shop by ID")
    @GetMapping("/{shopId}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable UUID shopId) {
        return ResponseEntity.ok(merchantService.getShopById(shopId));
    }

    @Operation(summary = "Update shop details")
    @PutMapping("/{shopId}")
    public ResponseEntity<ShopResponse> updateShop(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID shopId,
            @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.ok(merchantService.updateShop(userId, shopId, request));
    }

    @Operation(summary = "Toggle shop active/inactive status")
    @PatchMapping("/{shopId}/toggle")
    public ResponseEntity<Void> toggleShop(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID shopId) {
        merchantService.toggleShopStatus(userId, shopId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Upload shop logo or cover image")
    @PostMapping("/{shopId}/images")
    public ResponseEntity<ShopResponse> uploadImage(
            @RequestHeader("X-User-Id") UUID userId,
            @PathVariable UUID shopId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "logo") String imageType) {
        return ResponseEntity.ok(merchantService.uploadShopImage(userId, shopId, file, imageType));
    }

    @Operation(summary = "Get nearby shops (GPS-based)")
    @GetMapping("/nearby")
    public ResponseEntity<List<ShopResponse>> getNearbyShops(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm) {
        return ResponseEntity.ok(merchantService.getNearbyShops(latitude, longitude, radiusKm));
    }

    @Operation(summary = "Get nearby shops filtered by category")
    @GetMapping("/nearby/category")
    public ResponseEntity<List<ShopResponse>> getNearbyByCategory(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm,
            @RequestParam String category) {
        return ResponseEntity.ok(merchantService.getNearbyShopsByCategory(latitude, longitude, radiusKm, category));
    }
}
