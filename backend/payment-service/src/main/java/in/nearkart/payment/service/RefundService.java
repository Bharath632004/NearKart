package in.nearkart.payment.service;

import in.nearkart.payment.dto.request.RefundRequest;
import in.nearkart.payment.dto.response.RefundResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface RefundService {
    RefundResponse initiateRefund(RefundRequest request);
    void initiateAutoRefund(UUID orderId, BigDecimal amount, String reason);
    List<RefundResponse> getRefundsByOrderId(UUID orderId);
}
