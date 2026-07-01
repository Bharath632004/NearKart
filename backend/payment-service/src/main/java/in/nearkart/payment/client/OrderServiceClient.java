package in.nearkart.payment.client;

import in.nearkart.payment.client.dto.OrderSummaryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

/**
 * Feign client that calls order-service to fetch order details.
 * The service name "order-service" must match the spring.application.name
 * registered in Eureka / discovery-server.
 */
@FeignClient(name = "order-service", path = "/api/v1/orders")
public interface OrderServiceClient {

    /**
     * Returns a lightweight summary of the order (id + totalAmount).
     * order-service must expose GET /api/v1/orders/{orderId}/summary
     */
    @GetMapping("/{orderId}/summary")
    OrderSummaryResponse getOrderSummary(@PathVariable("orderId") UUID orderId);
}
