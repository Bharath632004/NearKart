package in.nearkart.payment.service.impl;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.nearkart.payment.dto.request.RefundRequest;
import in.nearkart.payment.dto.response.RefundResponse;
import in.nearkart.payment.entity.*;
import in.nearkart.payment.exception.PaymentNotFoundException;
import in.nearkart.payment.kafka.event.RefundInitiatedEvent;
import in.nearkart.payment.kafka.producer.PaymentEventProducer;
import in.nearkart.payment.repository.PaymentRepository;
import in.nearkart.payment.repository.RefundRepository;
import in.nearkart.payment.service.RefundService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundServiceImpl implements RefundService {

    private final PaymentRepository  paymentRepository;
    private final RefundRepository   refundRepository;
    private final RazorpayClient     razorpayClient;
    private final PaymentEventProducer eventProducer;

    @Override
    public RefundResponse initiateRefund(RefundRequest request) {
        Payment payment = paymentRepository.findByOrderId(request.getOrderId())
                .orElseThrow(() -> new PaymentNotFoundException(
                        "No payment found for orderId: " + request.getOrderId()));

        return processRefund(payment, request.getAmount(),
                request.getRefundMethod(), request.getReason());
    }

    @Override
    public void initiateAutoRefund(UUID orderId, BigDecimal amount, String reason) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            if (payment.getStatus() == PaymentStatus.SUCCESS) {
                processRefund(payment, amount, RefundMethod.ORIGINAL_PAYMENT_METHOD, reason);
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByOrderId(UUID orderId) {
        return refundRepository.findByOrderId(orderId)
                .stream().map(this::toResponse).toList();
    }

    // ----------------------------------------------------------------
    // PRIVATE: Call Razorpay Refund API
    // ----------------------------------------------------------------
    private RefundResponse processRefund(Payment payment, BigDecimal amount,
                                         RefundMethod method, String reason) {
        int amountInPaise = amount.multiply(BigDecimal.valueOf(100)).intValue();

        JSONObject refundOptions = new JSONObject();
        refundOptions.put("amount", amountInPaise);
        refundOptions.put("speed",  "normal");   // or "optimum"
        refundOptions.put("notes",  new JSONObject().put("reason", reason));

        String rzpRefundId = null;
        RefundStatus status = RefundStatus.PROCESSING;

        try {
            com.razorpay.Refund rzpRefund = razorpayClient.payments
                    .refund(payment.getRazorpayPaymentId(), refundOptions);
            rzpRefundId = rzpRefund.get("id");
            log.info("Razorpay refund created: rzpRefundId={}, orderId={}",
                    rzpRefundId, payment.getOrderId());
        } catch (RazorpayException e) {
            log.error("Razorpay refund API failed: {}", e.getMessage());
            status = RefundStatus.FAILED;
        }

        Refund refund = Refund.builder()
                .payment(payment)
                .orderId(payment.getOrderId())
                .amount(amount)
                .razorpayRefundId(rzpRefundId)
                .status(status)
                .refundMethod(method)
                .reason(reason)
                .build();

        if (status == RefundStatus.PROCESSING) {
            payment.setStatus(PaymentStatus.REFUND_INITIATED);
            paymentRepository.save(payment);
        }

        Refund saved = refundRepository.save(refund);

        // Publish Kafka → notification-service
        eventProducer.publishRefundInitiated(RefundInitiatedEvent.builder()
                .refundId(saved.getId())
                .orderId(saved.getOrderId())
                .customerId(payment.getCustomerId())
                .refundAmount(amount)
                .refundMethod(method.name())
                .initiatedAt(LocalDateTime.now())
                .build());

        return toResponse(saved);
    }

    private RefundResponse toResponse(Refund r) {
        return RefundResponse.builder()
                .id(r.getId()).orderId(r.getOrderId())
                .amount(r.getAmount()).status(r.getStatus())
                .refundMethod(r.getRefundMethod())
                .razorpayRefundId(r.getRazorpayRefundId())
                .reason(r.getReason()).createdAt(r.getCreatedAt())
                .build();
    }
}
