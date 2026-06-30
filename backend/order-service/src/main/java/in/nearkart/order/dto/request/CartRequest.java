package in.nearkart.order.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.UUID;

@Data
public class CartRequest {

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID productId;

    @NotNull
    private UUID shopId;

    @NotNull
    @Min(0)
    @Max(50)
    private Integer quantity; // 0 = remove from cart
}
