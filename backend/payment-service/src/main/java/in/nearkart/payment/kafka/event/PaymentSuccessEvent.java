package in.nearkart.payment.kafka.event;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID paymentId;
    private UUID orderId;
    private UUID customerId;
    private BigDecimal amount;
    private String razorpayPaymentId;
    private LocalDateTime paidAt;
}
