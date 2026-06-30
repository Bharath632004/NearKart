package in.nearkart.order.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nearkart.order.entity.OrderStatus;
import in.nearkart.order.service.OrderService;
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

    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            topics = "${nearkart.kafka.topics.payment-success}",
            groupId = "${nearkart.kafka.consumer.group-id}"
    )
    public void onPaymentSuccess(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> payload = objectMapper.readValue(record.value(), Map.class);
            UUID orderId = UUID.fromString((String) payload.get("orderId"));
            orderService.confirmOrderAfterPayment(orderId);
            log.info("Payment success processed for orderId={}", orderId);
        } catch (Exception e) {
            log.error("Error processing payment-success event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
            topics = "${nearkart.kafka.topics.payment-failed}",
            groupId = "${nearkart.kafka.consumer.group-id}"
    )
    public void onPaymentFailed(ConsumerRecord<String, String> record) {
        try {
            Map<?, ?> payload = objectMapper.readValue(record.value(), Map.class);
            UUID orderId = UUID.fromString((String) payload.get("orderId"));
            orderService.cancelOrderOnPaymentFailure(orderId);
            log.warn("Payment failed, order cancelled: orderId={}", orderId);
        } catch (Exception e) {
            log.error("Error processing payment-failed event: {}", e.getMessage(), e);
        }
    }
}
