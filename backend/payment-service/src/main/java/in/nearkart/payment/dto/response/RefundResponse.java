package in.nearkart.payment.dto.response;

import in.nearkart.payment.entity.RefundMethod;
import in.nearkart.payment.entity.RefundStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class RefundResponse {
    private UUID id;
    private UUID orderId;
    private BigDecimal amount;
    private RefundStatus status;
    private RefundMethod refundMethod;
    private String razorpayRefundId;
    private String reason;
    private LocalDateTime createdAt;
}
