package com.nearkart.inventoryservice.service;

import com.nearkart.inventoryservice.dto.*;
import com.nearkart.inventoryservice.exception.DuplicateInventoryException;
import com.nearkart.inventoryservice.exception.InsufficientStockException;
import com.nearkart.inventoryservice.exception.InventoryNotFoundException;
import com.nearkart.inventoryservice.model.*;
import com.nearkart.inventoryservice.repository.InventoryItemRepository;
import com.nearkart.inventoryservice.repository.StockTransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryItemRepository inventoryItemRepository;
    private final StockTransactionRepository stockTransactionRepository;

    @Value("${inventory.stock.low-threshold-default:10}")
    private int defaultLowStockThreshold;

    // ── Create ─────────────────────────────────────────────────────────────────

    @Transactional
    public InventoryItemResponse createInventoryItem(InventoryItemRequest request) {
        if (inventoryItemRepository.existsByProductIdAndShopId(request.getProductId(), request.getShopId())) {
            throw new DuplicateInventoryException(
                    "Inventory already exists for product " + request.getProductId()
                    + " in shop " + request.getShopId());
        }

        int threshold = request.getLowStockThreshold() != null
                ? request.getLowStockThreshold()
                : defaultLowStockThreshold;

        InventoryItem item = InventoryItem.builder()
                .productId(request.getProductId())
                .shopId(request.getShopId())
                .productName(request.getProductName())
                .sku(request.getSku())
                .quantityAvailable(request.getQuantityAvailable())
                .lowStockThreshold(threshold)
                .price(request.getPrice())
                .status(InventoryStatus.ACTIVE)
                .build();

        InventoryItem saved = inventoryItemRepository.save(item);
        log.info("Created inventory item id={} for product={} shop={}",
                saved.getId(), saved.getProductId(), saved.getShopId());

        if (request.getQuantityAvailable() > 0) {
            recordTransaction(saved.getId(), TransactionType.STOCK_IN,
                    request.getQuantityAvailable(), 0, request.getQuantityAvailable(),
                    null, "Initial stock");
        }

        return mapToResponse(saved);
    }

    // ── Read ───────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryItemById(Long id) {
        return mapToResponse(findItemById(id));
    }

    @Transactional(readOnly = true)
    public InventoryItemResponse getInventoryByProductAndShop(Long productId, Long shopId) {
        InventoryItem item = inventoryItemRepository.findByProductIdAndShopId(productId, shopId)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory not found for product " + productId + " in shop " + shopId));
        return mapToResponse(item);
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventoryByShop(Long shopId) {
        return inventoryItemRepository.findByShopId(shopId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getInventoryByProduct(Long productId) {
        return inventoryItemRepository.findByProductId(productId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    /**
     * Returns only ACTIVE items that are below their low-stock threshold
     * and still have quantity > 0.  OUT_OF_STOCK items are excluded because
     * they already trigger a separate alert path.
     */
    @Transactional(readOnly = true)
    public List<InventoryItemResponse> getLowStockItems(Long shopId) {
        List<InventoryItem> items = (shopId != null)
                ? inventoryItemRepository.findLowStockActiveItemsByShop(shopId)
                : inventoryItemRepository.findAllLowStockActiveItems();
        return items.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    // ── Stock operations ───────────────────────────────────────────────────────

    @Transactional
    public InventoryItemResponse updateStock(Long id, StockUpdateRequest request) {
        InventoryItem item = findItemById(id);
        int before = item.getQuantityAvailable();

        switch (request.getTransactionType()) {
            case STOCK_IN, RETURNED, RELEASED ->
                    item.setQuantityAvailable(before + request.getQuantity());
            case STOCK_OUT, RESERVED -> {
                if (before < request.getQuantity()) {
                    throw new InsufficientStockException(
                            "Insufficient stock. Available: " + before
                            + ", Requested: " + request.getQuantity());
                }
                item.setQuantityAvailable(before - request.getQuantity());
            }
            case ADJUSTMENT -> item.setQuantityAvailable(request.getQuantity());
        }

        // Auto-update status based on resulting quantity
        if (item.getQuantityAvailable() == 0) {
            item.setStatus(InventoryStatus.OUT_OF_STOCK);
        } else if (item.getStatus() == InventoryStatus.OUT_OF_STOCK) {
            item.setStatus(InventoryStatus.ACTIVE);
        }

        InventoryItem updated = inventoryItemRepository.save(item);
        recordTransaction(id, request.getTransactionType(), request.getQuantity(),
                before, updated.getQuantityAvailable(),
                request.getReferenceId(), request.getNotes());

        log.info("Stock updated for item id={}: {} -> {}", id, before, updated.getQuantityAvailable());
        return mapToResponse(updated);
    }

    // ── Update ─────────────────────────────────────────────────────────────────

    @Transactional
    public InventoryItemResponse updateInventoryDetails(Long id, InventoryItemRequest request) {
        InventoryItem item = findItemById(id);

        // Guard against SKU collision with a different item
        if (request.getSku() != null
                && !request.getSku().equals(item.getSku())
                && inventoryItemRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new DuplicateInventoryException("SKU '" + request.getSku() + "' is already in use");
        }

        if (request.getProductName() != null) item.setProductName(request.getProductName());
        if (request.getSku() != null)         item.setSku(request.getSku());
        if (request.getPrice() != null)       item.setPrice(request.getPrice());
        if (request.getLowStockThreshold() != null) item.setLowStockThreshold(request.getLowStockThreshold());

        return mapToResponse(inventoryItemRepository.save(item));
    }

    @Transactional
    public InventoryItemResponse updateStatus(Long id, InventoryStatus status) {
        InventoryItem item = findItemById(id);
        item.setStatus(status);
        return mapToResponse(inventoryItemRepository.save(item));
    }

    // ── Delete ─────────────────────────────────────────────────────────────────

    @Transactional
    public void deleteInventoryItem(Long id) {
        InventoryItem item = findItemById(id);
        inventoryItemRepository.delete(item);
        log.info("Deleted inventory item id={}", id);
    }

    // ── Stock check ────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public StockCheckResponse checkStock(StockCheckRequest request) {
        return inventoryItemRepository
                .findByProductIdAndShopId(request.getProductId(), request.getShopId())
                .map(item -> {
                    boolean available = item.getStatus() == InventoryStatus.ACTIVE
                            && item.getQuantityAvailable() >= request.getRequiredQuantity();
                    return StockCheckResponse.builder()
                            .productId(request.getProductId())
                            .shopId(request.getShopId())
                            .isAvailable(available)
                            .availableQuantity(item.getQuantityAvailable())
                            .requestedQuantity(request.getRequiredQuantity())
                            .message(available ? "Stock available" : "Insufficient stock or item inactive")
                            .build();
                })
                .orElse(StockCheckResponse.builder()
                        .productId(request.getProductId())
                        .shopId(request.getShopId())
                        .isAvailable(false)
                        .availableQuantity(0)
                        .requestedQuantity(request.getRequiredQuantity())
                        .message("Inventory not found")
                        .build());
    }

    // ── Transactions ───────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<StockTransactionResponse> getTransactionHistory(Long inventoryItemId) {
        return stockTransactionRepository
                .findByInventoryItemIdOrderByCreatedAtDesc(inventoryItemId)
                .stream().map(this::mapTransactionToResponse).collect(Collectors.toList());
    }

    // ── Private helpers ────────────────────────────────────────────────────────

    private InventoryItem findItemById(Long id) {
        return inventoryItemRepository.findById(id)
                .orElseThrow(() -> new InventoryNotFoundException(
                        "Inventory item not found with id: " + id));
    }

    private void recordTransaction(Long itemId, TransactionType type, int qty,
                                   int before, int after,
                                   String referenceId, String notes) {
        StockTransaction tx = StockTransaction.builder()
                .inventoryItemId(itemId)
                .transactionType(type)
                .quantity(qty)
                .quantityBefore(before)
                .quantityAfter(after)
                .referenceId(referenceId)
                .notes(notes)
                .performedBy(getCurrentUsername())
                .build();
        stockTransactionRepository.save(tx);
    }

    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated()) ? auth.getName() : "system";
    }

    private InventoryItemResponse mapToResponse(InventoryItem item) {
        return InventoryItemResponse.builder()
                .id(item.getId())
                .productId(item.getProductId())
                .shopId(item.getShopId())
                .productName(item.getProductName())
                .sku(item.getSku())
                .quantityAvailable(item.getQuantityAvailable())
                .lowStockThreshold(item.getLowStockThreshold())
                .price(item.getPrice())
                .status(item.getStatus())
                .isLowStock(item.getQuantityAvailable() != null
                        && item.getLowStockThreshold() != null
                        && item.getQuantityAvailable() <= item.getLowStockThreshold())
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private StockTransactionResponse mapTransactionToResponse(StockTransaction tx) {
        return StockTransactionResponse.builder()
                .id(tx.getId())
                .inventoryItemId(tx.getInventoryItemId())
                .transactionType(tx.getTransactionType())
                .quantity(tx.getQuantity())
                .quantityBefore(tx.getQuantityBefore())
                .quantityAfter(tx.getQuantityAfter())
                .referenceId(tx.getReferenceId())
                .notes(tx.getNotes())
                .performedBy(tx.getPerformedBy())
                .createdAt(tx.getCreatedAt())
                .build();
    }
}
