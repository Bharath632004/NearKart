package in.nearkart.payment.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import in.nearkart.payment.client.OrderServiceClient;
import in.nearkart.payment.client.dto.OrderSummaryResponse;
import in.nearkart.payment.config.RazorpayConfig;
import in.nearkart.payment.dto.request.CreatePaymentOrderRequest;
import in.nearkart.payment.dto.request.VerifyPaymentRequest;
import in.nearkart.payment.dto.response.PaymentOrderResponse;
import in.nearkart.payment.dto.response.PaymentResponse;
import in.nearkart.payment.entity.Payment;
import in.nearkart.payment.entity.PaymentStatus;
import in.nearkart.payment.exception.PaymentException;
import in.nearkart.payment.exception.PaymentNotFoundException;
import in.nearkart.payment.exception.SignatureVerificationException;
import in.nearkart.payment.kafka.event.PaymentFailedEvent;
import in.nearkart.payment.kafka.event.PaymentSuccessEvent;
import in.nearkart.payment.kafka.producer.PaymentEventProducer;
import in.nearkart.payment.repository.PaymentRepository;
import in.nearkart.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final RazorpayClient        razorpayClient;
    private final RazorpayConfig        razorpayConfig;
    private final PaymentRepository     paymentRepository;
    private final PaymentEventProducer  eventProducer;
    private final OrderServiceClient    orderServiceClient;

    // ----------------------------------------------------------------
    // STEP 1 – Create Razorpay Order (Frontend calls this first)
    // ----------------------------------------------------------------
    @Override
    public PaymentOrderResponse createRazorpayOrder(UUID customerId, CreatePaymentOrderRequest request) {

        // Fetch real order total from order-service via Feign
        OrderSummaryResponse orderSummary;
        try {
            orderSummary = orderServiceClient.getOrderSummary(request.getOrderId());
        } catch (Exception e) {
            log.error("Failed to fetch order summary for orderId={}: {}", request.getOrderId(), e.getMessage());
            throw new PaymentException("Could not retrieve order details. Please try again.");
        }

        BigDecimal amount = orderSummary.getTotalAmount();
        String     currency = orderSummary.getCurrency() != null ? orderSummary.getCurrency() : "INR";

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentException("Invalid order amount received from order-service: " + amount);
        }

        // Razorpay expects amount in paise (1 INR = 100 paise)
        int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject orderOptions = new JSONObject();
        orderOptions.put("amount",   amountInPaise);
        orderOptions.put("currency", currency);
        orderOptions.put("receipt",  "NK-" + request.getOrderId());

        try {
            Order razorpayOrder = razorpayClient.orders.create(orderOptions);
            String rzpOrderId   = razorpayOrder.get("id");

            // Persist payment record
            Payment payment = Payment.builder()
                    .orderId(request.getOrderId())
                    .customerId(customerId)
                    .amount(amount)
                    .method(request.getMethod())
                    .status(PaymentStatus.CREATED)
                    .razorpayOrderId(rzpOrderId)
                    .build();

            Payment saved = paymentRepository.save(payment);
            log.info("Razorpay order created: rzpOrderId={}, orderId={}, amount={} {}",
                    rzpOrderId, request.getOrderId(), amount, currency);

            return PaymentOrderResponse.builder()
                    .paymentId(saved.getId())
                    .razorpayOrderId(rzpOrderId)
                    .amount(amount)
                    .currency(currency)
                    .keyId(razorpayConfig.getKeyId())
                    .receipt("NK-" + request.getOrderId())
                    .build();

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new PaymentException("Could not create Razorpay order: " + e.getMessage());
        }
    }

    // ----------------------------------------------------------------
    // STEP 2 – Verify signature + capture (Frontend calls after payment)
    // ----------------------------------------------------------------
    @Override
    public PaymentResponse verifyAndCapturePayment(VerifyPaymentRequest request) {

        Payment payment = paymentRepository
                .findByRazorpayOrderId(request.getRazorpayOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "Payment not found for rzpOrderId: " + request.getRazorpayOrderId()));

        // Verify HMAC-SHA256 signature
        boolean valid = verifySignature(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        if (!valid) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason("Signature verification failed");
            paymentRepository.save(payment);

            eventProducer.publishPaymentFailed(PaymentFailedEvent.builder()
                    .paymentId(payment.getId())
                    .orderId(payment.getOrderId())
                    .customerId(payment.getCustomerId())
                    .failureReason("Signature verification failed")
                    .failedAt(LocalDateTime.now())
                    .build());

            throw new SignatureVerificationException("Razorpay signature is invalid");
        }

        // Update payment as SUCCESS
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setRazorpayPaymentId(request.getRazorpayPaymentId());
        payment.setRazorpaySignature(request.getRazorpaySignature());
        payment.setPaidAt(LocalDateTime.now());

        Map<String, Object> gatewayResp = new HashMap<>();
        gatewayResp.put("razorpay_order_id",   request.getRazorpayOrderId());
        gatewayResp.put("razorpay_payment_id", request.getRazorpayPaymentId());
        gatewayResp.put("verified_at",         LocalDateTime.now().toString());
        payment.setGatewayResponse(gatewayResp);

        Payment saved = paymentRepository.save(payment);
        log.info("Payment verified & captured: orderId={}, rzpPayId={}",
                payment.getOrderId(), request.getRazorpayPaymentId());

        // Publish → order-service will confirm order
        eventProducer.publishPaymentSuccess(PaymentSuccessEvent.builder()
                .paymentId(saved.getId())
                .orderId(saved.getOrderId())
                .customerId(saved.getCustomerId())
                .amount(saved.getAmount())
                .razorpayPaymentId(request.getRazorpayPaymentId())
                .paidAt(saved.getPaidAt())
                .build());

        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // GET PAYMENT
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(UUID orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found for orderId: " + orderId));
        return toResponse(payment);
    }

    // ----------------------------------------------------------------
    // RAZORPAY WEBHOOK (server-to-server verification)
    // ----------------------------------------------------------------
    @Override
    public void handleWebhook(String payload, String razorpaySignature) {
        try {
            boolean valid = Utils.verifyWebhookSignature(
                    payload, razorpaySignature, razorpayConfig.getKeySecret());

            if (!valid) {
                log.warn("Invalid webhook signature received");
                return;
            }

            JSONObject event    = new JSONObject(payload);
            String eventType    = event.getString("event");
            JSONObject entity   = event.getJSONObject("payload")
                                       .getJSONObject("payment")
                                       .getJSONObject("entity");

            String rzpPaymentId = entity.getString("id");
            String rzpOrderId   = entity.getString("order_id");

            log.info("Razorpay webhook received: event={}, paymentId={}", eventType, rzpPaymentId);

            switch (eventType) {
                case "payment.captured" -> handleWebhookCapture(rzpOrderId, rzpPaymentId);
                case "payment.failed"   -> handleWebhookFailed(rzpOrderId, entity.optString("error_description"));
                default -> log.debug("Unhandled webhook event: {}", eventType);
            }
        } catch (Exception e) {
            log.error("Webhook processing error: {}", e.getMessage(), e);
        }
    }

    // ----------------------------------------------------------------
    // PRIVATE HELPERS
    // ----------------------------------------------------------------
    private boolean verifySignature(String rzpOrderId, String rzpPaymentId, String signature) {
        try {
            String data = rzpOrderId + "|" + rzpPaymentId;
            return Utils.verifySignature(data, signature, razorpayConfig.getKeySecret());
        } catch (RazorpayException e) {
            log.error("Signature verification threw exception: {}", e.getMessage());
            return false;
        }
    }

    private void handleWebhookCapture(String rzpOrderId, String rzpPaymentId) {
        paymentRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(p -> {
            if (p.getStatus() != PaymentStatus.SUCCESS) {
                p.setStatus(PaymentStatus.SUCCESS);
                p.setRazorpayPaymentId(rzpPaymentId);
                p.setPaidAt(LocalDateTime.now());
                paymentRepository.save(p);
                eventProducer.publishPaymentSuccess(PaymentSuccessEvent.builder()
                        .paymentId(p.getId()).orderId(p.getOrderId())
                        .customerId(p.getCustomerId()).amount(p.getAmount())
                        .razorpayPaymentId(rzpPaymentId).paidAt(p.getPaidAt()).build());
            }
        });
    }

    private void handleWebhookFailed(String rzpOrderId, String reason) {
        paymentRepository.findByRazorpayOrderId(rzpOrderId).ifPresent(p -> {
            if (p.getStatus() != PaymentStatus.FAILED) {
                p.setStatus(PaymentStatus.FAILED);
                p.setFailureReason(reason);
                paymentRepository.save(p);
                eventProducer.publishPaymentFailed(PaymentFailedEvent.builder()
                        .paymentId(p.getId()).orderId(p.getOrderId())
                        .customerId(p.getCustomerId()).failureReason(reason)
                        .failedAt(LocalDateTime.now()).build());
            }
        });
    }

    private PaymentResponse toResponse(Payment p) {
        return PaymentResponse.builder()
                .id(p.getId()).orderId(p.getOrderId())
                .amount(p.getAmount()).currency(p.getCurrency())
                .status(p.getStatus()).method(p.getMethod())
                .razorpayOrderId(p.getRazorpayOrderId())
                .razorpayPaymentId(p.getRazorpayPaymentId())
                .paidAt(p.getPaidAt()).createdAt(p.getCreatedAt())
                .build();
    }
}
