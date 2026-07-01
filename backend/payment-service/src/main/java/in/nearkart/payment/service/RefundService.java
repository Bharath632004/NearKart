package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.RefundRequest;
import in.nearkart.payment.dto.response.RefundResponse;
import in.nearkart.payment.entity.RefundStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RefundService {
    RefundResponse initiateRefund(UUID paymentId, RefundRequest request);
    List<RefundResponse> getRefundsByPaymentId(UUID paymentId);
    RefundResponse getRefundById(UUID refundId);
    RefundResponse updateRefundStatus(String rzpRefundId, RefundStatus status);
    void initiateAutoRefund(UUID paymentId, BigDecimal amount, String reason);
}
