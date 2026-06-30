package in.nearkart.order.client;

import in.nearkart.order.client.dto.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "product-service", path = "/api/v1/products")
public interface ProductServiceClient {

    @GetMapping("/{productId}")
    ProductResponse getProductById(@PathVariable UUID productId);
}
