package in.nearkart.payment.controller;

import in.nearkart.payment.dto.request.CreatePaymentOrderRequest;
import in.nearkart.payment.dto.request.VerifyPaymentRequest;
import in.nearkart.payment.dto.response.ApiResponse;
import in.nearkart.payment.dto.response.PaymentOrderResponse;
import in.nearkart.payment.dto.response.PaymentResponse;
import in.nearkart.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Step 1 – Create Razorpay order and return credentials to frontend.
     */
    @PostMapping("/create-order")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentOrderResponse>> createOrder(
            @RequestHeader("X-User-Id") UUID customerId,
            @Valid @RequestBody CreatePaymentOrderRequest request) {
        PaymentOrderResponse resp = paymentService.createRazorpayOrder(customerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Razorpay order created", resp));
    }

    /**
     * Step 2 – Frontend sends back the 3 Razorpay fields for server-side verification.
     */
    @PostMapping("/verify")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<ApiResponse<PaymentResponse>> verifyPayment(
            @Valid @RequestBody VerifyPaymentRequest request) {
        PaymentResponse resp = paymentService.verifyAndCapturePayment(request);
        return ResponseEntity.ok(ApiResponse.success("Payment verified successfully", resp));
    }

    /**
     * Get payment status for an order.
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByOrderId(
            @PathVariable UUID orderId) {
        return ResponseEntity.ok(
                ApiResponse.success("Payment fetched", paymentService.getPaymentByOrderId(orderId)));
    }

    /**
     * Razorpay server-to-server webhook endpoint.
     * Must be whitelisted in Razorpay Dashboard → Webhooks.
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {
        paymentService.handleWebhook(payload, signature);
        return ResponseEntity.ok().build();
    }
}
