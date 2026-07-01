package in.nearkart.payment.kafka.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.nearkart.payment.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final RefundService refundService;
    private final ObjectMapper objectMapper;

    /**
     * When order-service emits order.cancelled -> auto-trigger refund
     */
    @KafkaListener(
            topics = "${nearkart.kafka.topics.order-cancelled}",
            groupId = "${nearkart.kafka.consumer.group-id}"
    )
    public void onOrderCancelled(ConsumerRecord<String, String> record) {
        try {
            // Use Map<String, Object> instead of Map<?,?> to avoid wildcard capture error
            @SuppressWarnings("unchecked")
            Map<String, Object> payload = objectMapper.readValue(record.value(), Map.class);

            UUID orderId      = UUID.fromString((String) payload.get("orderId"));
            BigDecimal amount = new BigDecimal(payload.get("refundAmount").toString());
            String reason     = (String) payload.getOrDefault("cancellationReason", "Order cancelled");

            refundService.initiateAutoRefund(orderId, amount, reason);
            log.info("Auto-refund triggered for cancelled orderId={}", orderId);
        } catch (Exception e) {
            log.error("Error processing order-cancelled event: {}", e.getMessage(), e);
        }
    }
}
