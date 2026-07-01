package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.RefundRequest;
import in.nearkart.payment.dto.response.RefundResponse;
import in.nearkart.payment.entity.RefundStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RefundService {

    // Initiate a refund for a given paymentId
    RefundResponse initiateRefund(UUID paymentId, RefundRequest request);

    // List refunds for a given paymentId
    List<RefundResponse> getRefundsByPaymentId(UUID paymentId);

    // Get a single refund by its id
    RefundResponse getRefundById(UUID refundId);

    // Update refund status based on Razorpay refund id
    RefundResponse updateRefundStatus(String rzpRefundId, RefundStatus status);

    // Auto-refund used by OrderEventConsumer (to fix initiateAutoRefund error)
    void initiateAutoRefund(UUID paymentId, BigDecimal amount, String reason);
}
