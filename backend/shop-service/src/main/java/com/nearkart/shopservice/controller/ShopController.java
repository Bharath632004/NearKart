package com.nearkart.shopservice.controller;

import com.nearkart.shopservice.dto.ShopRequest;
import com.nearkart.shopservice.dto.ShopResponse;
import com.nearkart.shopservice.model.ShopCategory;
import com.nearkart.shopservice.service.ShopService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
@Tag(name = "Shops", description = "Shop management — create, search, verify, nearby")
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Register a new shop")
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody ShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shopService.createShop(request));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get shop by ID")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all shops (admin only)")
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @GetMapping("/active")
    @Operation(summary = "Get active shops (paginated)")
    public ResponseEntity<Page<ShopResponse>> getActiveShops(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(shopService.getActiveShopsPaged(pageable));
    }

    @GetMapping("/merchant/{merchantId}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get all shops for a merchant")
    public ResponseEntity<List<ShopResponse>> getByMerchant(@PathVariable Long merchantId) {
        return ResponseEntity.ok(shopService.getShopsByMerchant(merchantId));
    }

    @GetMapping("/merchant/{merchantId}/paged")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Get paginated shops for a merchant")
    public ResponseEntity<Page<ShopResponse>> getByMerchantPaged(
            @PathVariable Long merchantId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(shopService.getShopsByMerchantPaged(merchantId, pageable));
    }

    @GetMapping("/merchant/{merchantId}/count")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Count shops for a merchant")
    public ResponseEntity<Long> countByMerchant(@PathVariable Long merchantId) {
        return ResponseEntity.ok(shopService.countShopsByMerchant(merchantId));
    }

    @GetMapping("/city/{city}")
    @Operation(summary = "Get active shops in a city")
    public ResponseEntity<List<ShopResponse>> getByCity(@PathVariable String city) {
        return ResponseEntity.ok(shopService.getShopsByCity(city));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "Get active shops by category")
    public ResponseEntity<List<ShopResponse>> getByCategory(@PathVariable ShopCategory category) {
        return ResponseEntity.ok(shopService.getShopsByCategory(category));
    }

    @GetMapping("/nearby")
    @Operation(summary = "Get nearby active shops within radius (km)")
    public ResponseEntity<List<ShopResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {
        return ResponseEntity.ok(shopService.getNearbyShops(lat, lng, radius));
    }

    @GetMapping("/nearby/category/{category}")
    @Operation(summary = "Get nearby active shops by category within radius (km)")
    public ResponseEntity<List<ShopResponse>> getNearbyByCategory(
            @PathVariable ShopCategory category,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {
        return ResponseEntity.ok(shopService.getNearbyShopsByCategory(lat, lng, radius, category));
    }

    @GetMapping("/search")
    @Operation(summary = "Search active shops by name keyword")
    public ResponseEntity<List<ShopResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(shopService.searchShops(keyword));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Update shop details")
    public ResponseEntity<ShopResponse> updateShop(@PathVariable Long id,
                                                    @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.ok(shopService.updateShop(id, request));
    }

    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Toggle shop active/inactive")
    public ResponseEntity<ShopResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.toggleShopActive(id));
    }

    @PatchMapping("/{id}/verify")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Verify a shop — Admin only")
    public ResponseEntity<ShopResponse> verifyShop(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.verifyShop(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SELLER','ADMIN')")
    @Operation(summary = "Soft-delete (deactivate) a shop")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
