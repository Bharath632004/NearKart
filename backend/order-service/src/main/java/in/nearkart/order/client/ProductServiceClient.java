package in.nearkart.order.client;

import in.nearkart.order.client.dto.ProductResponse;
import in.nearkart.order.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
    name = "product-service",
    url = "${services.product-service.url:http://product-service}",
    configuration = FeignConfig.class
)
public interface ProductServiceClient {

    @GetMapping("/api/v1/products/{productId}")
    ProductResponse getProductById(@PathVariable UUID productId);
}
