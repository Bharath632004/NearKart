package in.nearkart.payment.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Returned to the frontend so it can open Razorpay checkout.
 */
@Data
@Builder
public class PaymentOrderResponse {
    private UUID paymentId;           // Internal DB id
    private String razorpayOrderId;   // order_XXXXXXXXX  – pass to Razorpay JS SDK
    private BigDecimal amount;        // in INR
    private String currency;
    private String keyId;             // Razorpay key_id (public)
    private String receipt;           // order number for reference
}
