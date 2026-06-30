package com.nearkart.analytics.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nearkart.analytics.dto.OrderEvent;
import com.nearkart.analytics.service.AnalyticsAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final AnalyticsAggregationService aggregationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "analytics-service-group")
    public void consumeOrderEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("Received order event: orderId={}, status={}", event.getOrderId(), event.getStatus());
            aggregationService.processOrderEvent(event);
        } catch (Exception e) {
            log.error("Failed to process order event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "delivery-events", groupId = "analytics-service-group")
    public void consumeDeliveryEvent(String message) {
        try {
            OrderEvent event = objectMapper.readValue(message, OrderEvent.class);
            log.info("Received delivery event: orderId={}", event.getOrderId());
            aggregationService.processDeliveryEvent(event);
        } catch (Exception e) {
            log.error("Failed to process delivery event: {}", e.getMessage(), e);
        }
    }
}
