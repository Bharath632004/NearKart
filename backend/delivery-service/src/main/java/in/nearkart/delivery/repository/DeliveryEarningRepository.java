import in.nearkart.delivery.entity.DeliveryEarning;
import in.nearkart.delivery.entity.DeliveryPartner;
import in.nearkart.delivery.dto.response.DeliveryEarningResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public List<DeliveryEarningResponse> getEarningsForPartner(DeliveryPartner partner, Pageable pageable) {
    List<DeliveryEarning> earnings =
            earningRepository.findByPartnerOrderByCreatedAtDesc(partner, pageable);

    return earnings.stream()
            .map(this::toResponse)
            .toList();   // or .collect(Collectors.toList())
}
