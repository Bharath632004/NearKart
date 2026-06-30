package in.nearkart.payment.dto.request;

import in.nearkart.payment.entity.RefundMethod;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class RefundRequest {

    @NotNull
    private UUID orderId;

    @NotNull
    @DecimalMin(value = "1.00", message = "Refund amount must be at least ₹1")
    private BigDecimal amount;

    @NotNull
    private RefundMethod refundMethod;

    private String reason;
}
