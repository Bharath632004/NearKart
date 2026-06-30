package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.CreatePaymentOrderRequest;
import in.nearkart.payment.dto.request.VerifyPaymentRequest;
import in.nearkart.payment.dto.response.PaymentOrderResponse;
import in.nearkart.payment.dto.response.PaymentResponse;

import java.util.UUID;

public interface PaymentService {
    PaymentOrderResponse createRazorpayOrder(UUID customerId, CreatePaymentOrderRequest request);
    PaymentResponse verifyAndCapturePayment(VerifyPaymentRequest request);
    PaymentResponse getPaymentByOrderId(UUID orderId);
    void handleWebhook(String payload, String razorpaySignature);
}
