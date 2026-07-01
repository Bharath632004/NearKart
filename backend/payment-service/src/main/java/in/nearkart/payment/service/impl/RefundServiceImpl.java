package in.nearkart.payment.service.impl;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.nearkart.payment.dto.request.RefundRequest;
import in.nearkart.payment.dto.response.RefundResponse;
import in.nearkart.payment.entity.*;
import in.nearkart.payment.exception.PaymentException;
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
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class RefundServiceImpl implements RefundService {

    private final RazorpayClient razorpayClient;
    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentEventProducer eventProducer;

    @Override
    public RefundResponse initiateRefund(UUID paymentId, RefundRequest request) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new PaymentException("Cannot refund payment in status: " + payment.getStatus());
        }

        if (payment.getRazorpayPaymentId() == null) {
            throw new PaymentException("No Razorpay payment ID found for payment: " + paymentId);
        }

        int amountInPaise = request.getAmount()
                .multiply(java.math.BigDecimal.valueOf(100)).intValue();

        JSONObject refundOptions = new JSONObject();
        refundOptions.put("amount", amountInPaise);
        refundOptions.put("speed", "normal");
        refundOptions.put("notes", new JSONObject().put("reason", request.getReason()));

        try {
            com.razorpay.Refund rzpRefund = razorpayClient.payments
                    .refund(payment.getRazorpayPaymentId(), refundOptions);

            String rzpRefundId = rzpRefund.get("id");

            Refund refund = Refund.builder()
                    .paymentId(paymentId)
                    .orderId(payment.getOrderId())
                    .amount(request.getAmount())
                    .reason(request.getReason())
                    .refundMethod(RefundMethod.ORIGINAL_SOURCE)
                    .status(RefundStatus.INITIATED)
                    .razorpayRefundId(rzpRefundId)
                    .initiatedAt(LocalDateTime.now())
                    .build();

            Refund saved = refundRepository.save(refund);
            log.info("Refund initiated: refundId={}, rzpRefundId={}", saved.getId(), rzpRefundId);

            eventProducer.publishRefundInitiated(RefundInitiatedEvent.builder()
                    .refundId(saved.getId())
                    .paymentId(paymentId)
                    .orderId(payment.getOrderId())
                    .customerId(payment.getCustomerId())
                    .amount(request.getAmount())
                    .reason(request.getReason())
                    .initiatedAt(saved.getInitiatedAt())
                    .build());

            return toResponse(saved);

        } catch (RazorpayException e) {
            log.error("Razorpay refund failed: {}", e.getMessage());
            throw new PaymentException("Refund failed: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefundResponse> getRefundsByPaymentId(UUID paymentId) {
        return refundRepository.findByPaymentId(paymentId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RefundResponse getRefundById(UUID refundId) {
        return refundRepository.findById(refundId)
                .map(this::toResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Refund not found: " + refundId));
    }

    @Override
    public RefundResponse updateRefundStatus(String rzpRefundId, RefundStatus status) {
        Refund refund = refundRepository.findByRazorpayRefundId(rzpRefundId)
                .orElseThrow(() -> new PaymentNotFoundException("Refund not found: " + rzpRefundId));
        refund.setStatus(status);
        if (status == RefundStatus.PROCESSED) {
            refund.setProcessedAt(LocalDateTime.now());
        }
        return toResponse(refundRepository.save(refund));
    }

    @Override
    public void initiateAutoRefund(UUID paymentId, BigDecimal amount, String reason) {
        RefundRequest request = new RefundRequest();
        request.setAmount(amount);
        request.setReason(reason);
        initiateRefund(paymentId, request);
    }

    private RefundResponse toResponse(Refund r) {
        return RefundResponse.builder()
                .id(r.getId())
                .orderId(r.getOrderId())
                .amount(r.getAmount())
                .reason(r.getReason())
                .status(r.getStatus())
                .refundMethod(r.getRefundMethod())
                .razorpayRefundId(r.getRazorpayRefundId())
                .createdAt(r.getCreatedAt())
                .build();
    }
}
