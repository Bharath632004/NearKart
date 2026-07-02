package com.nearkart.inventoryservice.service;

import com.nearkart.inventoryservice.dto.*;
import com.nearkart.inventoryservice.exception.DuplicateInventoryException;
import com.nearkart.inventoryservice.exception.InsufficientStockException;
import com.nearkart.inventoryservice.exception.InventoryNotFoundException;
import com.nearkart.inventoryservice.model.*;
import com.nearkart.inventoryservice.repository.InventoryItemRepository;
import com.nearkart.inventoryservice.repository.StockTransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private InventoryItemRepository inventoryItemRepository;
    @Mock private StockTransactionRepository stockTransactionRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(inventoryService, "defaultLowStockThreshold", 10);
    }

    // ── Test 1: createInventoryItem success ───────────────────────────────────
    @Test
    void createInventoryItem_success() {
        InventoryItemRequest req = InventoryItemRequest.builder()
                .productId(1L).shopId(2L)
                .productName("Apple").sku("SKU-001")
                .quantityAvailable(100).price(BigDecimal.valueOf(50))
                .build();

        when(inventoryItemRepository.existsByProductIdAndShopId(1L, 2L)).thenReturn(false);

        InventoryItem saved = InventoryItem.builder()
                .id(1L).productId(1L).shopId(2L)
                .productName("Apple").sku("SKU-001")
                .quantityAvailable(100).lowStockThreshold(10)
                .price(BigDecimal.valueOf(50))
                .status(InventoryStatus.ACTIVE)
                .build();
        when(inventoryItemRepository.save(any())).thenReturn(saved);
        when(stockTransactionRepository.save(any())).thenReturn(null);

        InventoryItemResponse resp = inventoryService.createInventoryItem(req);

        assertThat(resp.getStatus()).isEqualTo(InventoryStatus.ACTIVE);
        assertThat(resp.getQuantityAvailable()).isEqualTo(100);
    }

    // ── Test 2: createInventoryItem duplicate throws ───────────────────────────
    @Test
    void createInventoryItem_duplicate_throws() {
        InventoryItemRequest req = InventoryItemRequest.builder()
                .productId(1L).shopId(2L).productName("Apple")
                .sku("SKU-001").quantityAvailable(10)
                .price(BigDecimal.ONE).build();

        when(inventoryItemRepository.existsByProductIdAndShopId(1L, 2L)).thenReturn(true);

        assertThatThrownBy(() -> inventoryService.createInventoryItem(req))
                .isInstanceOf(DuplicateInventoryException.class);
    }

    // ── Test 3: updateStock STOCK_OUT success ─────────────────────────────────
    @Test
    void updateStock_stockOut_success() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).quantityAvailable(50).lowStockThreshold(10)
                .status(InventoryStatus.ACTIVE).build();

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(stockTransactionRepository.save(any())).thenReturn(null);

        StockUpdateRequest req = StockUpdateRequest.builder()
                .transactionType(TransactionType.STOCK_OUT).quantity(20).build();

        InventoryItemResponse resp = inventoryService.updateStock(1L, req);

        assertThat(resp.getQuantityAvailable()).isEqualTo(30);
        assertThat(resp.getStatus()).isEqualTo(InventoryStatus.ACTIVE);
    }

    // ── Test 4: updateStock STOCK_OUT insufficient throws ─────────────────────
    @Test
    void updateStock_insufficientStock_throws() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).quantityAvailable(5).lowStockThreshold(10)
                .status(InventoryStatus.ACTIVE).build();

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));

        StockUpdateRequest req = StockUpdateRequest.builder()
                .transactionType(TransactionType.STOCK_OUT).quantity(10).build();

        assertThatThrownBy(() -> inventoryService.updateStock(1L, req))
                .isInstanceOf(InsufficientStockException.class)
                .hasMessageContaining("Insufficient stock");
    }

    // ── Test 5: updateStock sets OUT_OF_STOCK when qty reaches 0 ─────────────
    @Test
    void updateStock_setsOutOfStock_whenQtyZero() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).quantityAvailable(5).lowStockThreshold(10)
                .status(InventoryStatus.ACTIVE).build();

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(stockTransactionRepository.save(any())).thenReturn(null);

        StockUpdateRequest req = StockUpdateRequest.builder()
                .transactionType(TransactionType.STOCK_OUT).quantity(5).build();

        InventoryItemResponse resp = inventoryService.updateStock(1L, req);

        assertThat(resp.getQuantityAvailable()).isEqualTo(0);
        assertThat(resp.getStatus()).isEqualTo(InventoryStatus.OUT_OF_STOCK);
    }

    // ── Test 6: getInventoryItemById not found throws ─────────────────────────
    @Test
    void getInventoryItemById_notFound_throws() {
        when(inventoryItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inventoryService.getInventoryItemById(99L))
                .isInstanceOf(InventoryNotFoundException.class)
                .hasMessageContaining("not found");
    }

    // ── Test 7: updateInventoryDetails duplicate SKU throws ───────────────────
    @Test
    void updateInventoryDetails_duplicateSku_throws() {
        InventoryItem item = InventoryItem.builder()
                .id(1L).productName("Apple").sku("SKU-001")
                .price(BigDecimal.TEN).lowStockThreshold(10)
                .quantityAvailable(20).status(InventoryStatus.ACTIVE).build();

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(inventoryItemRepository.existsBySkuAndIdNot("SKU-002", 1L)).thenReturn(true);

        InventoryItemRequest req = InventoryItemRequest.builder()
                .productName("Apple").sku("SKU-002")
                .price(BigDecimal.TEN).lowStockThreshold(10)
                .quantityAvailable(20).productId(1L).shopId(2L).build();

        assertThatThrownBy(() -> inventoryService.updateInventoryDetails(1L, req))
                .isInstanceOf(DuplicateInventoryException.class)
                .hasMessageContaining("SKU");
    }
}
