package in.nearkart.payment.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Lightweight DTO returned by order-service GET /api/v1/orders/{id}/summary.
 * Only the fields payment-service actually needs are mapped here.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSummaryResponse {

    private UUID   orderId;

    /** Final payable amount including taxes, discounts, delivery charges. */
    private BigDecimal totalAmount;

    /** ISO-4217 currency code, e.g. "INR". */
    private String currency;
}
