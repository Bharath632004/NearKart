package in.nearkart.payment.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WalletTopUpRequest {

    @NotNull
    @DecimalMin(value = "10.00", message = "Minimum top-up is ₹10")
    private BigDecimal amount;

    private String razorpayPaymentId;   // After Razorpay top-up flow
}
