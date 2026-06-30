package in.nearkart.order.kafka.producer;

import in.nearkart.order.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${nearkart.kafka.topics.order-placed}")
    private String orderPlacedTopic;

    @Value("${nearkart.kafka.topics.order-status-changed}")
    private String orderStatusChangedTopic;

    @Value("${nearkart.kafka.topics.order-cancelled}")
    private String orderCancelledTopic;

    public void publishOrderPlaced(OrderPlacedEvent event) {
        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(orderPlacedTopic, event.getOrderId().toString(), event);

        future.whenComplete((result, ex) -> {
            if (ex != null) {
                log.error("Failed to publish OrderPlacedEvent for orderId={}: {}",
                        event.getOrderId(), ex.getMessage());
            } else {
                log.info("OrderPlacedEvent published: orderId={}, partition={}, offset={}",
                        event.getOrderId(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            }
        });
    }

    public void publishOrderStatusChanged(OrderStatusChangedEvent event) {
        kafkaTemplate.send(orderStatusChangedTopic, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderStatusChangedEvent: {}", ex.getMessage());
                    } else {
                        log.info("OrderStatusChangedEvent published: orderId={}, status={}",
                                event.getOrderId(), event.getNewStatus());
                    }
                });
    }

    public void publishOrderCancelled(OrderCancelledEvent event) {
        kafkaTemplate.send(orderCancelledTopic, event.getOrderId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish OrderCancelledEvent: {}", ex.getMessage());
                    } else {
                        log.info("OrderCancelledEvent published: orderId={}", event.getOrderId());
                    }
                });
    }
}
