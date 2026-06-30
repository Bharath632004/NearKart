package in.nearkart.order.kafka.consumer;

import in.nearkart.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
        topics = "${kafka.topics.payment-success:payment.success}",
        groupId = "${spring.kafka.consumer.group-id:order-service-group}"
    )
    public void handlePaymentSuccess(Map<String, Object> event) {
        try {
            UUID orderId = UUID.fromString((String) event.get("orderId"));
            log.info("Payment success received for orderId={}", orderId);
            orderService.confirmOrderAfterPayment(orderId);
        } catch (Exception e) {
            log.error("Error handling payment success event: {}", e.getMessage(), e);
        }
    }

    @KafkaListener(
        topics = "${kafka.topics.payment-failed:payment.failed}",
        groupId = "${spring.kafka.consumer.group-id:order-service-group}"
    )
    public void handlePaymentFailed(Map<String, Object> event) {
        try {
            UUID orderId = UUID.fromString((String) event.get("orderId"));
            log.info("Payment failed received for orderId={}", orderId);
            orderService.cancelOrderOnPaymentFailure(orderId);
        } catch (Exception e) {
            log.error("Error handling payment failed event: {}", e.getMessage(), e);
        }
    }
}
