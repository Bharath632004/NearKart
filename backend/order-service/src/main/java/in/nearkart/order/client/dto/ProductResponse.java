package in.nearkart.order.client.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private UUID id;
    private String name;
    private String imageUrl;
    private BigDecimal price;
    private boolean available;
    private UUID shopId;
}
