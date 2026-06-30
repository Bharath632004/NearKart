package in.nearkart.delivery.service;

import in.nearkart.delivery.dto.request.UpdateAssignmentStatusRequest;
import in.nearkart.delivery.dto.request.VerifyOtpRequest;
import in.nearkart.delivery.dto.response.AssignmentResponse;
import in.nearkart.delivery.kafka.event.OrderReadyForPickupEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeliveryAssignmentService {
    AssignmentResponse autoAssign(OrderReadyForPickupEvent event);
    AssignmentResponse manualAssign(UUID orderId, UUID partnerId);
    AssignmentResponse getByOrderId(UUID orderId);
    AssignmentResponse getById(UUID assignmentId);
    AssignmentResponse updateStatus(UUID assignmentId, UpdateAssignmentStatusRequest request, UUID partnerId);
    boolean verifyDeliveryOtp(VerifyOtpRequest request, UUID partnerId);
    boolean verifyPickupOtp(VerifyOtpRequest request, UUID partnerId);
    Page<AssignmentResponse> getPartnerAssignments(UUID partnerId, Pageable pageable);
}
