package in.nearkart.order.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull
    @Min(value = 1, message = "Quantity must be at least 1")
    @Max(value = 50, message = "Maximum 50 units per item")
    private Integer quantity;
}
