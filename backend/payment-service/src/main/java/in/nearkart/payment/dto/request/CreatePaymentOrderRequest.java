package in.nearkart.payment.dto.request;

import in.nearkart.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class CreatePaymentOrderRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod method;
}
