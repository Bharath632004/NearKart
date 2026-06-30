package in.nearkart.order.kafka.producer;

import in.nearkart.order.kafka.event.OrderCancelledEvent;
import in.nearkart.order.kafka.event.OrderPlacedEvent;
import in.nearkart.order.kafka.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.order-placed:order.placed}")
    private String orderPlacedTopic;

    @Value("${kafka.topics.order-status-changed:order.status.changed}")
    private String orderStatusChangedTopic;

    @Value("${kafka.topics.order-cancelled:order.cancelled}")
    private String orderCancelledTopic;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent: orderId={}", event.getOrderId());
        kafkaTemplate.send(orderPlacedTopic, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderPlacedEvent: {}", ex.getMessage());
                    } else {
                        log.debug("OrderPlacedEvent published to partition {}",
                                result.getRecordMetadata().partition());
                    }
                });
    }

    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        log.info("Publishing OrderStatusChangedEvent: orderId={}, {} -> {}",
                event.getOrderId(), event.getPreviousStatus(), event.getNewStatus());
        kafkaTemplate.send(orderStatusChangedTopic, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderStatusChangedEvent: {}", ex.getMessage());
                    }
                });
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        log.info("Publishing OrderCancelledEvent: orderId={}", event.getOrderId());
        kafkaTemplate.send(orderCancelledTopic, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCancelledEvent: {}", ex.getMessage());
                    }
                });
    }
}
