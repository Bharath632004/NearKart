package in.nearkart.delivery.kafka.consumer;

import in.nearkart.delivery.kafka.event.OrderReadyForPickupEvent;
import in.nearkart.delivery.service.DeliveryAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final DeliveryAssignmentService assignmentService;

    /**
     * Listens for orders that are ready for pickup (merchant has prepared them).
     * Auto-assigns the nearest available delivery partner.
     */
    @KafkaListener(
            topics = "order.ready-for-pickup",
            groupId = "delivery-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void onOrderReadyForPickup(OrderReadyForPickupEvent event) {
        log.info("[Kafka] Received order.ready-for-pickup: orderId={}, orderNumber={}",
                event.getOrderId(), event.getOrderNumber());
        try {
            assignmentService.autoAssign(event);
        } catch (Exception ex) {
            log.error("Auto-assign failed for orderId={}: {}", event.getOrderId(), ex.getMessage());
            // TODO: Push to dead-letter topic or alert admin
        }
    }
}
