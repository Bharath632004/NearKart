package in.nearkart.payment.kafka.producer;

import in.nearkart.payment.kafka.event.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${nearkart.kafka.topics.payment-success}")  private String successTopic;
    @Value("${nearkart.kafka.topics.payment-failed}")   private String failedTopic;
    @Value("${nearkart.kafka.topics.refund-initiated}") private String refundTopic;

    public void publishPaymentSuccess(PaymentSuccessEvent event) {
        kafkaTemplate.send(successTopic, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("PaymentSuccessEvent publish failed: {}", ex.getMessage());
                    else log.info("PaymentSuccessEvent published: orderId={}", event.getOrderId());
                });
    }

    public void publishPaymentFailed(PaymentFailedEvent event) {
        kafkaTemplate.send(failedTopic, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("PaymentFailedEvent publish failed: {}", ex.getMessage());
                    else log.warn("PaymentFailedEvent published: orderId={}", event.getOrderId());
                });
    }

    public void publishRefundInitiated(RefundInitiatedEvent event) {
        kafkaTemplate.send(refundTopic, event.getOrderId().toString(), event)
                .whenComplete((r, ex) -> {
                    if (ex != null) log.error("RefundInitiatedEvent publish failed: {}", ex.getMessage());
                    else log.info("RefundInitiatedEvent published: orderId={}", event.getOrderId());
                });
    }
}
