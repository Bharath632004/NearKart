package in.nearkart.delivery.service.impl;

import in.nearkart.delivery.dto.response.EarningResponse;
import in.nearkart.delivery.entity.DeliveryEarning;
import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.exception.PartnerNotFoundException;
import in.nearkart.delivery.repository.DeliveryEarningRepository;
import in.nearkart.delivery.repository.DeliveryPartnerRepository;
import in.nearkart.delivery.service.DeliveryEarningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeliveryEarningServiceImpl implements DeliveryEarningService {

    private final DeliveryEarningRepository earningRepository;
    private final DeliveryPartnerRepository partnerRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<EarningResponse> getEarningsByPartner(UUID partnerId, Pageable pageable) {
        DeliveryPartner partner = findPartnerOrThrow(partnerId);
        return earningRepository.findByPartnerOrderByCreatedAtDesc(partner, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getUnsettledBalance(UUID partnerId) {
        DeliveryPartner partner = findPartnerOrThrow(partnerId);
        BigDecimal balance = earningRepository.sumUnsettledByPartner(partner);
        return balance != null ? balance : BigDecimal.ZERO;
    }

    @Override
    public void settleEarnings(UUID partnerId) {
        DeliveryPartner partner = findPartnerOrThrow(partnerId);
        List<DeliveryEarning> unsettled = earningRepository.findByPartnerAndSettled(partner, false);
        unsettled.forEach(e -> e.setSettled(true));
        earningRepository.saveAll(unsettled);
        log.info("Settled {} earnings for partner {}", unsettled.size(), partnerId);
    }

    private DeliveryPartner findPartnerOrThrow(UUID partnerId) {
        return partnerRepository.findById(partnerId)
                .orElseThrow(() -> new PartnerNotFoundException("Partner not found: " + partnerId));
    }

    private EarningResponse toResponse(DeliveryEarning e) {
        return EarningResponse.builder()
                .id(e.getId())
                .partnerId(e.getPartner().getId())
                .assignmentId(e.getAssignmentId())
                .amount(e.getAmount())
                .description(e.getDescription())
                .settled(e.isSettled())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
