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
public class PaymentEventConsumer {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${nearkart.kafka.topics.payment-success}",
                   groupId = "${nearkart.kafka.consumer.group-id}")
    public void onPaymentSuccess(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> p = objectMapper.readValue(record.value(), Map.class);
            UUID customerId = UUID.fromString((String) p.get("customerId"));
            String amount   = p.get("amount").toString();
            String orderId  = (String) p.get("orderId");

            notificationService.send(SendNotificationRequest.builder()
                    .userId(customerId).type(NotificationType.PAYMENT_SUCCESS)
                    .channel(NotificationChannel.PUSH)
                    .referenceId(UUID.fromString(orderId)).referenceType("PAYMENT")
                    .templateArgs(new String[]{amount, orderId})
                    .build());
        } catch (Exception e) {
            log.error("onPaymentSuccess notification error: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${nearkart.kafka.topics.payment-failed}",
                   groupId = "${nearkart.kafka.consumer.group-id}")
    public void onPaymentFailed(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> p = objectMapper.readValue(record.value(), Map.class);
            UUID customerId = UUID.fromString((String) p.get("customerId"));
            String orderId  = (String) p.get("orderId");

            notificationService.send(SendNotificationRequest.builder()
                    .userId(customerId).type(NotificationType.PAYMENT_FAILED)
                    .channel(NotificationChannel.PUSH)
                    .referenceId(UUID.fromString(orderId)).referenceType("PAYMENT")
                    .templateArgs(new String[]{orderId})
                    .build());
        } catch (Exception e) {
            log.error("onPaymentFailed notification error: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(topics = "${nearkart.kafka.topics.refund-initiated}",
                   groupId = "${nearkart.kafka.consumer.group-id}")
    public void onRefundInitiated(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> p = objectMapper.readValue(record.value(), Map.class);
            UUID customerId = UUID.fromString((String) p.get("customerId"));
            String amount   = p.get("refundAmount").toString();
            String orderId  = (String) p.get("orderId");

            notificationService.send(SendNotificationRequest.builder()
                    .userId(customerId).type(NotificationType.REFUND_INITIATED)
                    .channel(NotificationChannel.PUSH)
                    .referenceId(UUID.fromString(orderId)).referenceType("REFUND")
                    .templateArgs(new String[]{amount, orderId})
                    .build());
        } catch (Exception e) {
            log.error("onRefundInitiated notification error: {}", e.getMessage(), e);
        }
    }
}
