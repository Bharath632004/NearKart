package com.nearkart.inventoryservice.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.inventory-low-stock:inventory-low-stock}")
    private String lowStockTopic;

    @Value("${kafka.topic.inventory-out-of-stock:inventory-out-of-stock}")
    private String outOfStockTopic;

    public void publishLowStockAlert(UUID productId, String productName, int currentStock, int threshold) {
        Map<String, Object> event = Map.of(
                "eventType", "LOW_STOCK_ALERT",
                "productId", productId.toString(),
                "productName", productName,
                "currentStock", currentStock,
                "threshold", threshold,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(lowStockTopic, productId.toString(), event);
        log.info("Published LOW_STOCK_ALERT for product {} (stock: {})", productId, currentStock);
    }

    public void publishOutOfStockEvent(UUID productId, String productName) {
        Map<String, Object> event = Map.of(
                "eventType", "OUT_OF_STOCK",
                "productId", productId.toString(),
                "productName", productName,
                "timestamp", LocalDateTime.now().toString()
        );
        kafkaTemplate.send(outOfStockTopic, productId.toString(), event);
        log.info("Published OUT_OF_STOCK event for product {}", productId);
    }
}
