package in.nearkart.delivery.service;

import in.nearkart.delivery.dto.request.RegisterPartnerRequest;
import in.nearkart.delivery.dto.request.UpdateLocationRequest;
import in.nearkart.delivery.dto.response.PartnerResponse;
import in.nearkart.delivery.entity.PartnerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeliveryPartnerService {
    PartnerResponse register(RegisterPartnerRequest request);
    PartnerResponse getById(UUID partnerId);
    PartnerResponse updateStatus(UUID partnerId, PartnerStatus status);
    PartnerResponse updateLocation(UUID partnerId, UpdateLocationRequest request);
    void approveKyc(UUID partnerId);
    Page<PartnerResponse> listByStatus(PartnerStatus status, Pageable pageable);
    Page<PartnerResponse> listAll(Pageable pageable);
}
