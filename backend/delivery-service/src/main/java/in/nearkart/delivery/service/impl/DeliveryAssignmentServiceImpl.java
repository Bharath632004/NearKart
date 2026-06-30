package in.nearkart.delivery.service.impl;

import in.nearkart.delivery.dto.request.UpdateAssignmentStatusRequest;
import in.nearkart.delivery.dto.request.VerifyOtpRequest;
import in.nearkart.delivery.dto.response.AssignmentResponse;
import in.nearkart.delivery.entity.*;
import in.nearkart.delivery.exception.*;
import in.nearkart.delivery.kafka.event.DeliveryAssignedEvent;
import in.nearkart.delivery.kafka.event.DeliveryStatusChangedEvent;
import in.nearkart.delivery.kafka.event.OrderReadyForPickupEvent;
import in.nearkart.delivery.kafka.producer.DeliveryEventProducer;
import in.nearkart.delivery.repository.*;
import in.nearkart.delivery.service.DeliveryAssignmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryAssignmentServiceImpl implements DeliveryAssignmentService {

    private final DeliveryAssignmentRepository assignmentRepository;
    private final DeliveryPartnerRepository partnerRepository;
    private final DeliveryEarningRepository earningRepository;
    private final DeliveryEventProducer eventProducer;

    @Value("${nearkart.delivery.max-auto-assign-radius-km:5}")
    private double maxRadiusKm;

    @Value("${nearkart.delivery.otp-expiry-minutes:10}")
    private int otpExpiryMinutes;

    private static final BigDecimal BASE_DELIVERY_EARNING = new BigDecimal("30.00");
    private static final SecureRandom RANDOM = new SecureRandom();

    // ----------------------------------------------------------------
    // AUTO-ASSIGN (triggered by Kafka event)
    // ----------------------------------------------------------------
    @Override
    public AssignmentResponse autoAssign(OrderReadyForPickupEvent event) {
        if (assignmentRepository.findByOrderId(event.getOrderId()).isPresent()) {
            throw new AssignmentAlreadyExistsException("Order already assigned: " + event.getOrderId());
        }

        List<DeliveryPartner> nearbyPartners = partnerRepository.findNearbyOnlinePartners(
                event.getShopLatitude(), event.getShopLongitude(), maxRadiusKm, 5);

        if (nearbyPartners.isEmpty()) {
            log.warn("No online partners found near shopId={} for orderId={}",
                    event.getShopId(), event.getOrderId());
            throw new NoPartnerAvailableException("No available delivery partners nearby");
        }

        // Pick the nearest partner (first result from Haversine-ordered query)
        DeliveryPartner partner = nearbyPartners.get(0);

        return createAssignment(event, partner);
    }

    // ----------------------------------------------------------------
    // MANUAL ASSIGN (admin)
    // ----------------------------------------------------------------
    @Override
    public AssignmentResponse manualAssign(UUID orderId, UUID partnerId) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));

        if (assignmentRepository.findByOrderId(orderId).isPresent()) {
            throw new AssignmentAlreadyExistsException("Order already assigned: " + orderId);
        }

        // Build a minimal event for re-use
        OrderReadyForPickupEvent event = OrderReadyForPickupEvent.builder()
                .orderId(orderId)
                .orderNumber("MANUAL-" + orderId)
                .shopId(null)
                .customerId(null)
                .shopLatitude(0.0)
                .shopLongitude(0.0)
                .build();

        return createAssignment(event, partner);
    }

    // ----------------------------------------------------------------
    // GET
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getByOrderId(UUID orderId) {
        return toResponse(assignmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found for order: " + orderId)));
    }

    @Override
    @Transactional(readOnly = true)
    public AssignmentResponse getById(UUID assignmentId) {
        return toResponse(findOrThrow(assignmentId));
    }

    // ----------------------------------------------------------------
    // UPDATE STATUS
    // ----------------------------------------------------------------
    @Override
    public AssignmentResponse updateStatus(UUID assignmentId,
                                           UpdateAssignmentStatusRequest request,
                                           UUID partnerId) {
        DeliveryAssignment assignment = findOrThrow(assignmentId);
        validatePartnerOwnsAssignment(assignment, partnerId);

        AssignmentStatus prev = assignment.getStatus();
        assignment.setStatus(request.getStatus());

        switch (request.getStatus()) {
            case ACCEPTED        -> {
                assignment.setAcceptedAt(LocalDateTime.now());
                assignment.getPartner().setStatus(PartnerStatus.ON_DELIVERY);
            }
            case PICKED_UP       -> assignment.setPickedUpAt(LocalDateTime.now());
            case DELIVERED       -> {
                assignment.setDeliveredAt(LocalDateTime.now());
                creditEarnings(assignment);
            }
            case FAILED          -> {
                assignment.setFailedAt(LocalDateTime.now());
                assignment.setFailureReason(request.getFailureReason());
                assignment.getPartner().setStatus(PartnerStatus.ONLINE);
            }
            case REJECTED        -> assignment.getPartner().setStatus(PartnerStatus.ONLINE);
            default              -> { /* no side-effect */ }
        }

        DeliveryAssignment saved = assignmentRepository.save(assignment);
        log.info("Assignment {} status: {} → {}", assignmentId, prev, request.getStatus());

        eventProducer.publishDeliveryStatusChanged(DeliveryStatusChangedEvent.builder()
                .assignmentId(saved.getId())
                .orderId(saved.getOrderId())
                .orderNumber(saved.getOrderNumber())
                .partnerId(saved.getPartner().getId())
                .partnerName(saved.getPartner().getFullName())
                .previousStatus(prev.name())
                .newStatus(saved.getStatus().name())
                .changedAt(LocalDateTime.now())
                .build());

        return toResponse(saved);
    }

    // ----------------------------------------------------------------
    // OTP VERIFICATION
    // ----------------------------------------------------------------
    @Override
    public boolean verifyDeliveryOtp(VerifyOtpRequest request, UUID partnerId) {
        DeliveryAssignment assignment = findOrThrow(request.getAssignmentId());
        validatePartnerOwnsAssignment(assignment, partnerId);

        if (assignment.getOtpExpiresAt().isBefore(LocalDateTime.now())) {
            throw new OtpExpiredException("Delivery OTP has expired");
        }
        if (!assignment.getDeliveryOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid delivery OTP");
        }

        assignment.setOtpVerified(true);
        assignmentRepository.save(assignment);
        log.info("Delivery OTP verified for assignment: {}", request.getAssignmentId());
        return true;
    }

    @Override
    public boolean verifyPickupOtp(VerifyOtpRequest request, UUID partnerId) {
        DeliveryAssignment assignment = findOrThrow(request.getAssignmentId());
        validatePartnerOwnsAssignment(assignment, partnerId);

        if (!assignment.getPickupOtp().equals(request.getOtp())) {
            throw new InvalidOtpException("Invalid pickup OTP");
        }

        assignment.setPickupOtpVerified(true);
        assignmentRepository.save(assignment);
        log.info("Pickup OTP verified for assignment: {}", request.getAssignmentId());
        return true;
    }

    // ----------------------------------------------------------------
    // PARTNER HISTORY
    // ----------------------------------------------------------------
    @Override
    @Transactional(readOnly = true)
    public Page<AssignmentResponse> getPartnerAssignments(UUID partnerId, Pageable pageable) {
        DeliveryPartner partner = partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));
        return assignmentRepository.findByPartnerOrderByCreatedAtDesc(partner, pageable)
                .map(this::toResponse);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------
    private AssignmentResponse createAssignment(OrderReadyForPickupEvent event, DeliveryPartner partner) {
        String deliveryOtp = generateOtp();
        String pickupOtp   = generateOtp();

        DeliveryAssignment assignment = DeliveryAssignment.builder()
                .orderId(event.getOrderId())
                .orderNumber(event.getOrderNumber())
                .partner(partner)
                .shopId(event.getShopId())
                .customerId(event.getCustomerId())
                .shopAddress(event.getShopAddress())
                .shopLatitude(event.getShopLatitude())
                .shopLongitude(event.getShopLongitude())
                .deliveryAddress(event.getDeliveryAddress())
                .deliveryLatitude(event.getDeliveryLatitude())
                .deliveryLongitude(event.getDeliveryLongitude())
                .deliveryOtp(deliveryOtp)
                .otpExpiresAt(LocalDateTime.now().plusMinutes(otpExpiryMinutes))
                .otpVerified(false)
                .pickupOtp(pickupOtp)
                .pickupOtpVerified(false)
                .deliveryFeeEarned(BASE_DELIVERY_EARNING)
                .status(AssignmentStatus.ASSIGNED)
                .build();

        partner.setStatus(PartnerStatus.ON_DELIVERY);
        DeliveryAssignment saved = assignmentRepository.save(assignment);

        log.info("Assignment created: orderId={}, partnerId={}, assignmentId={}",
                event.getOrderId(), partner.getId(), saved.getId());

        // Publish Kafka → notification-service, order-service
        eventProducer.publishDeliveryAssigned(DeliveryAssignedEvent.builder()
                .assignmentId(saved.getId())
                .orderId(saved.getOrderId())
                .orderNumber(saved.getOrderNumber())
                .partnerId(partner.getId())
                .partnerName(partner.getFullName())
                .partnerPhone(partner.getPhone())
                .customerId(event.getCustomerId())
                .shopId(event.getShopId())
                .deliveryOtp(deliveryOtp)
                .assignedAt(saved.getCreatedAt())
                .build());

        return toResponse(saved);
    }

    private void creditEarnings(DeliveryAssignment assignment) {
        DeliveryPartner partner = assignment.getPartner();
        BigDecimal earned = assignment.getDeliveryFeeEarned();

        partner.setWalletBalance(partner.getWalletBalance().add(earned));
        partner.setTotalEarnings(partner.getTotalEarnings().add(earned));
        partner.setTotalDeliveries(partner.getTotalDeliveries() + 1);
        partner.setStatus(PartnerStatus.ONLINE);

        earningRepository.save(DeliveryEarning.builder()
                .partner(partner)
                .assignmentId(assignment.getId())
                .amount(earned)
                .description("Delivery earning for order: " + assignment.getOrderNumber())
                .settled(false)
                .build());
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private void validatePartnerOwnsAssignment(DeliveryAssignment assignment, UUID partnerId) {
        if (!assignment.getPartner().getId().equals(partnerId)) {
            throw new UnauthorizedActionException("Partner does not own this assignment");
        }
    }

    private DeliveryAssignment findOrThrow(UUID id) {
        return assignmentRepository.findById(id)
                .orElseThrow(() -> new AssignmentNotFoundException("Assignment not found: " + id));
    }

    private AssignmentResponse toResponse(DeliveryAssignment a) {
        return AssignmentResponse.builder()
                .id(a.getId())
                .orderId(a.getOrderId())
                .orderNumber(a.getOrderNumber())
                .partnerId(a.getPartner().getId())
                .partnerName(a.getPartner().getFullName())
                .partnerPhone(a.getPartner().getPhone())
                .shopId(a.getShopId())
                .shopAddress(a.getShopAddress())
                .deliveryAddress(a.getDeliveryAddress())
                .status(a.getStatus())
                .deliveryFeeEarned(a.getDeliveryFeeEarned())
                .otpVerified(a.getOtpVerified())
                .pickupOtpVerified(a.getPickupOtpVerified())
                .acceptedAt(a.getAcceptedAt())
                .pickedUpAt(a.getPickedUpAt())
                .deliveredAt(a.getDeliveredAt())
                .createdAt(a.getCreatedAt())
                .build();
    }
}
