package in.nearkart.delivery.service.impl;

import in.nearkart.delivery.dto.request.RegisterPartnerRequest;
import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.dto.response.PartnerResponse;
import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.entity.PartnerStatus;
import in.nearkart.delivery.exception.DuplicateResourceException;
import in.nearkart.delivery.exception.PartnerNotFoundException;
import in.nearkart.delivery.repository.DeliveryPartnerRepository;
import in.nearkart.delivery.service.DeliveryPartnerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryPartnerServiceImpl implements DeliveryPartnerService {

    private final DeliveryPartnerRepository partnerRepository;

    @Override
    public PartnerResponse register(RegisterPartnerRequest request) {
        if (partnerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateResourceException("Phone already registered: " + request.getPhone());
        }
        if (partnerRepository.existsByAadhaarNumber(request.getAadhaarNumber())) {
            throw new DuplicateResourceException("Aadhaar already registered");
        }
        if (partnerRepository.existsByVehicleNumber(request.getVehicleNumber())) {
            throw new DuplicateResourceException("Vehicle number already registered: " + request.getVehicleNumber());
        }

        DeliveryPartner partner = DeliveryPartner.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .aadhaarNumber(request.getAadhaarNumber())
                .panNumber(request.getPanNumber())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleType(request.getVehicleType())
                .status(PartnerStatus.PENDING_KYC)
                .build();

        DeliveryPartner saved = partnerRepository.save(partner);
        log.info("Delivery partner registered: phone={}, id={}", saved.getPhone(), saved.getId());
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PartnerResponse getById(UUID partnerId) {
        return toResponse(findOrThrow(partnerId));
    }

    @Override
    public PartnerResponse updateStatus(UUID partnerId, PartnerStatus status) {
        DeliveryPartner partner = findOrThrow(partnerId);
        partner.setStatus(status);
        log.info("Partner {} status → {}", partnerId, status);
        return toResponse(partnerRepository.save(partner));
    }

    @Override
    public PartnerResponse updateLocation(UUID partnerId, UpdateLocationRequest request) {
        DeliveryPartner partner = findOrThrow(partnerId);
        partner.setCurrentLatitude(request.getLatitude());
        partner.setCurrentLongitude(request.getLongitude());
        return toResponse(partnerRepository.save(partner));
    }

    @Override
    public void approveKyc(UUID partnerId) {
        DeliveryPartner partner = findOrThrow(partnerId);
        partner.setIsKycVerified(true);
        partner.setStatus(PartnerStatus.ACTIVE);
        partnerRepository.save(partner);
        log.info("KYC approved for partner: {}", partnerId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerResponse> listByStatus(PartnerStatus status, Pageable pageable) {
        return partnerRepository.findByStatus(status, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PartnerResponse> listAll(Pageable pageable) {
        return partnerRepository.findAll(pageable).map(this::toResponse);
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------
    private DeliveryPartner findOrThrow(UUID id) {
        return partnerRepository.findById(id)
                .orElseThrow(() -> new PartnerNotFoundException("Delivery partner not found: " + id));
    }

    private PartnerResponse toResponse(DeliveryPartner p) {
        return PartnerResponse.builder()
                .id(p.getId())
                .fullName(p.getFullName())
                .phone(p.getPhone())
                .email(p.getEmail())
                .vehicleType(p.getVehicleType())
                .vehicleNumber(p.getVehicleNumber())
                .status(p.getStatus())
                .currentLatitude(p.getCurrentLatitude())
                .currentLongitude(p.getCurrentLongitude())
                .walletBalance(p.getWalletBalance())
                .totalEarnings(p.getTotalEarnings())
                .totalDeliveries(p.getTotalDeliveries())
                .averageRating(p.getAverageRating())
                .isKycVerified(p.getIsKycVerified())
                .createdAt(p.getCreatedAt())
                .build();
    }
}
