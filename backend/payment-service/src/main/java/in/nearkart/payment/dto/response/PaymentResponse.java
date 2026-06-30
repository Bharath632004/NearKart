package in.nearkart.payment.dto.response;

import in.nearkart.payment.entity.PaymentMethod;
import in.nearkart.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentStatus status;
    private PaymentMethod method;
    private String razorpayOrderId;
    private String razorpayPaymentId;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
}
