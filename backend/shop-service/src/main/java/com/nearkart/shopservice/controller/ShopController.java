package com.nearkart.shopservice.controller;

import com.nearkart.shopservice.dto.ShopRequest;
import com.nearkart.shopservice.dto.ShopResponse;
import com.nearkart.shopservice.service.ShopService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shops")
@RequiredArgsConstructor
public class ShopController {

    private final ShopService shopService;

    @PostMapping
    public ResponseEntity<ShopResponse> createShop(@Valid @RequestBody ShopRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(shopService.createShop(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShopResponse> getShop(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.getShopById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShopResponse>> getAllShops() {
        return ResponseEntity.ok(shopService.getAllShops());
    }

    @GetMapping("/merchant/{merchantId}")
    public ResponseEntity<List<ShopResponse>> getByMerchant(@PathVariable Long merchantId) {
        return ResponseEntity.ok(shopService.getShopsByMerchant(merchantId));
    }

    @GetMapping("/nearby")
    public ResponseEntity<List<ShopResponse>> getNearby(
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5.0") double radius) {
        return ResponseEntity.ok(shopService.getNearbyShops(lat, lng, radius));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ShopResponse>> search(@RequestParam String keyword) {
        return ResponseEntity.ok(shopService.searchShops(keyword));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShopResponse> updateShop(@PathVariable Long id,
                                                    @Valid @RequestBody ShopRequest request) {
        return ResponseEntity.ok(shopService.updateShop(id, request));
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ShopResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.toggleShopActive(id));
    }

    @PatchMapping("/{id}/verify")
    public ResponseEntity<ShopResponse> verifyShop(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.verifyShop(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteShop(@PathVariable Long id) {
        shopService.deleteShop(id);
        return ResponseEntity.noContent().build();
    }
}
