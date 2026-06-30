package com.nearkart.inventoryservice;

import com.nearkart.inventoryservice.dto.*;
import com.nearkart.inventoryservice.exception.InsufficientStockException;
import com.nearkart.inventoryservice.exception.InventoryNotFoundException;
import com.nearkart.inventoryservice.model.*;
import com.nearkart.inventoryservice.repository.InventoryItemRepository;
import com.nearkart.inventoryservice.repository.StockTransactionRepository;
import com.nearkart.inventoryservice.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryItemRepository inventoryItemRepository;

    @Mock
    private StockTransactionRepository stockTransactionRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private InventoryItem sampleItem;

    @BeforeEach
    void setUp() {
        sampleItem = InventoryItem.builder()
                .id(1L)
                .productId(100L)
                .shopId(200L)
                .productName("Test Product")
                .sku("SKU-001")
                .quantityAvailable(50)
                .lowStockThreshold(10)
                .price(new BigDecimal("99.99"))
                .status(InventoryStatus.ACTIVE)
                .build();
    }

    @Test
    void createInventoryItem_success() {
        InventoryItemRequest request = new InventoryItemRequest();
        request.setProductId(100L);
        request.setShopId(200L);
        request.setProductName("Test Product");
        request.setSku("SKU-001");
        request.setQuantityAvailable(50);
        request.setPrice(new BigDecimal("99.99"));

        when(inventoryItemRepository.existsByProductIdAndShopId(100L, 200L)).thenReturn(false);
        when(inventoryItemRepository.save(any())).thenReturn(sampleItem);
        when(stockTransactionRepository.save(any())).thenReturn(null);

        InventoryItemResponse response = inventoryService.createInventoryItem(request);

        assertThat(response.getProductId()).isEqualTo(100L);
        assertThat(response.getQuantityAvailable()).isEqualTo(50);
    }

    @Test
    void updateStock_stockOut_success() {
        StockUpdateRequest request = new StockUpdateRequest();
        request.setQuantity(10);
        request.setTransactionType(TransactionType.STOCK_OUT);

        InventoryItem updated = InventoryItem.builder()
                .id(1L).productId(100L).shopId(200L).productName("Test").sku("SKU-001")
                .quantityAvailable(40).lowStockThreshold(10).price(new BigDecimal("99.99"))
                .status(InventoryStatus.ACTIVE).build();

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));
        when(inventoryItemRepository.save(any())).thenReturn(updated);
        when(stockTransactionRepository.save(any())).thenReturn(null);

        InventoryItemResponse response = inventoryService.updateStock(1L, request);
        assertThat(response.getQuantityAvailable()).isEqualTo(40);
    }

    @Test
    void updateStock_insufficientStock_throws() {
        StockUpdateRequest request = new StockUpdateRequest();
        request.setQuantity(100);
        request.setTransactionType(TransactionType.STOCK_OUT);

        when(inventoryItemRepository.findById(1L)).thenReturn(Optional.of(sampleItem));

        assertThatThrownBy(() -> inventoryService.updateStock(1L, request))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void getInventoryItemById_notFound_throws() {
        when(inventoryItemRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> inventoryService.getInventoryItemById(99L))
                .isInstanceOf(InventoryNotFoundException.class);
    }

    @Test
    void checkStock_available() {
        StockCheckRequest request = new StockCheckRequest();
        request.setProductId(100L);
        request.setShopId(200L);
        request.setRequiredQuantity(20);

        when(inventoryItemRepository.findByProductIdAndShopId(100L, 200L)).thenReturn(Optional.of(sampleItem));

        StockCheckResponse response = inventoryService.checkStock(request);
        assertThat(response.isAvailable()).isTrue();
        assertThat(response.getAvailableQuantity()).isEqualTo(50);
    }

    @Test
    void getLowStockItems_byShop() {
        when(inventoryItemRepository.findLowStockItemsByShop(200L)).thenReturn(List.of(sampleItem));
        List<InventoryItemResponse> items = inventoryService.getLowStockItems(200L);
        assertThat(items).hasSize(1);
    }
}
