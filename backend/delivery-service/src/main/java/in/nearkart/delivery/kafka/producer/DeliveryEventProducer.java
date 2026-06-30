package in.nearkart.delivery.kafka.producer;

import in.nearkart.delivery.kafka.event.DeliveryAssignedEvent;
import in.nearkart.delivery.kafka.event.DeliveryStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliveryEventProducer {

    private static final String TOPIC_ASSIGNED       = "delivery.assigned";
    private static final String TOPIC_STATUS_CHANGED = "delivery.status-changed";

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDeliveryAssigned(DeliveryAssignedEvent event) {
        kafkaTemplate.send(TOPIC_ASSIGNED, event.getOrderId().toString(), event);
        log.info("[Kafka] Published delivery.assigned: orderId={}", event.getOrderId());
    }

    public void publishDeliveryStatusChanged(DeliveryStatusChangedEvent event) {
        kafkaTemplate.send(TOPIC_STATUS_CHANGED, event.getOrderId().toString(), event);
        log.info("[Kafka] Published delivery.status-changed: orderId={}, newStatus={}",
                event.getOrderId(), event.getNewStatus());
    }
}
