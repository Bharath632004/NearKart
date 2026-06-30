package in.nearkart.order.dto.request;

import in.nearkart.order.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
public class PlaceOrderRequest {

    @NotNull(message = "Shop ID is required")
    private UUID shopId;

    @NotNull(message = "Delivery address is required")
    private UUID deliveryAddressId;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    @NotEmpty(message = "Order must have at least one item")
    @Valid
    private List<OrderItemRequest> items;

    private Long couponId;

    @Size(max = 500)
    private String specialInstructions;
}
