package com.nearkart.inventoryservice.controller;

import com.nearkart.inventoryservice.dto.*;
import com.nearkart.inventoryservice.model.InventoryStatus;
import com.nearkart.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    // ---- Create ----
    @PostMapping
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> createInventoryItem(
            @Valid @RequestBody InventoryItemRequest request) {
        InventoryItemResponse response = inventoryService.createInventoryItem(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Inventory item created successfully", response));
    }

    // ---- Read ----
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getInventoryItemById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Inventory item fetched", inventoryService.getInventoryItemById(id)));
    }

    @GetMapping("/product/{productId}/shop/{shopId}")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> getByProductAndShop(
            @PathVariable Long productId, @PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success("Inventory fetched",
                inventoryService.getInventoryByProductAndShop(productId, shopId)));
    }

    @GetMapping("/shop/{shopId}")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventoryByShop(@PathVariable Long shopId) {
        return ResponseEntity.ok(ApiResponse.success("Shop inventory fetched",
                inventoryService.getInventoryByShop(shopId)));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getInventoryByProduct(@PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success("Product inventory fetched",
                inventoryService.getInventoryByProduct(productId)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<InventoryItemResponse>>> getLowStockItems(
            @RequestParam(required = false) Long shopId) {
        return ResponseEntity.ok(ApiResponse.success("Low stock items fetched",
                inventoryService.getLowStockItems(shopId)));
    }

    // ---- Stock Operations ----
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN', 'ORDER_SERVICE')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateStock(
            @PathVariable Long id, @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully",
                inventoryService.updateStock(id, request)));
    }

    @PostMapping("/check")
    public ResponseEntity<ApiResponse<StockCheckResponse>> checkStock(
            @Valid @RequestBody StockCheckRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Stock check completed",
                inventoryService.checkStock(request)));
    }

    // ---- Update ----
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateInventoryDetails(
            @PathVariable Long id, @Valid @RequestBody InventoryItemRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Inventory updated",
                inventoryService.updateInventoryDetails(id, request)));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<InventoryItemResponse>> updateStatus(
            @PathVariable Long id, @RequestParam InventoryStatus status) {
        return ResponseEntity.ok(ApiResponse.success("Status updated",
                inventoryService.updateStatus(id, status)));
    }

    // ---- Delete ----
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteInventoryItem(@PathVariable Long id) {
        inventoryService.deleteInventoryItem(id);
        return ResponseEntity.ok(ApiResponse.success("Inventory item deleted", null));
    }

    // ---- Transactions ----
    @GetMapping("/{id}/transactions")
    @PreAuthorize("hasAnyRole('MERCHANT', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> getTransactionHistory(
            @PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Transaction history fetched",
                inventoryService.getTransactionHistory(id)));
    }
}
