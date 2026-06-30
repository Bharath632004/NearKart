package in.nearkart.notification.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nearkart.notification.dto.request.SendNotificationRequest;
import in.nearkart.notification.entity.NotificationChannel;
import in.nearkart.notification.entity.NotificationType;
import in.nearkart.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // ---- ORDER PLACED ------------------------------------------------
    @KafkaListener(topics = "${nearkart.kafka.topics.order-placed}",
                   groupId = "${nearkart.kafka.consumer.group-id}")
    public void onOrderPlaced(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> p = objectMapper.readValue(record.value(), Map.class);
            UUID customerId  = UUID.fromString((String) p.get("customerId"));
            String orderNum  = (String) p.get("orderNumber");
            String total     = p.get("totalAmount").toString();

            notificationService.send(SendNotificationRequest.builder()
                    .userId(customerId).type(NotificationType.ORDER_PLACED)
                    .channel(NotificationChannel.PUSH)
                    .referenceId(UUID.fromString((String) p.get("orderId")))
                    .referenceType("ORDER")
                    .templateArgs(new String[]{orderNum, total})
                    .build());

            log.info("Order-placed notification sent: orderId={}", p.get("orderId"));
        } catch (Exception e) {
            log.error("onOrderPlaced error: {}", e.getMessage(), e);
        }
    }

    // ---- ORDER STATUS CHANGED ----------------------------------------
    @KafkaListener(topics = "${nearkart.kafka.topics.order-status-changed}",
                   groupId = "${nearkart.kafka.consumer.group-id}")
    public void onOrderStatusChanged(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> p = objectMapper.readValue(record.value(), Map.class);
            UUID customerId = UUID.fromString((String) p.get("customerId"));
            String orderNum = (String) p.get("orderNumber");
            String newStatus= (String) p.get("newStatus");

            NotificationType type = resolveOrderStatusType(newStatus);
            if (type == null) return;

            notificationService.send(SendNotificationRequest.builder()
                    .userId(customerId).type(type)
                    .channel(NotificationChannel.PUSH)
                    .referenceId(UUID.fromString((String) p.get("orderId")))
                    .referenceType("ORDER")
                    .templateArgs(new String[]{orderNum})
                    .build());
        } catch (Exception e) {
            log.error("onOrderStatusChanged error: {}", e.getMessage(), e);
        }
    }

    // ---- ORDER CANCELLED --------------------------------------------
    @KafkaListener(topics = "${nearkart.kafka.topics.order-cancelled}",
                   groupId = "${nearkart.kafka.consumer.group-id}")
    public void onOrderCancelled(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> p = objectMapper.readValue(record.value(), Map.class);
            UUID customerId = UUID.fromString((String) p.get("customerId"));
            String orderNum = (String) p.get("orderNumber");

            notificationService.send(SendNotificationRequest.builder()
                    .userId(customerId).type(NotificationType.ORDER_CANCELLED)
                    .channel(NotificationChannel.PUSH)
                    .referenceId(UUID.fromString((String) p.get("orderId")))
                    .referenceType("ORDER")
                    .templateArgs(new String[]{orderNum})
                    .build());
        } catch (Exception e) {
            log.error("onOrderCancelled error: {}", e.getMessage(), e);
        }
    }

    private NotificationType resolveOrderStatusType(String status) {
        return switch (status) {
            case "CONFIRMED"         -> NotificationType.ORDER_CONFIRMED;
            case "PREPARING"         -> NotificationType.ORDER_PREPARING;
            case "READY_FOR_PICKUP"  -> NotificationType.ORDER_READY_FOR_PICKUP;
            case "PICKED_UP"         -> NotificationType.ORDER_PICKED_UP;
            case "OUT_FOR_DELIVERY"  -> NotificationType.ORDER_OUT_FOR_DELIVERY;
            case "DELIVERED"         -> NotificationType.ORDER_DELIVERED;
            default                  -> null;
        };
    }
}
