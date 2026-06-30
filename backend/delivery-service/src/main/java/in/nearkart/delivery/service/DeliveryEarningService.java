package in.nearkart.delivery.service;

import in.nearkart.delivery.dto.response.EarningResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.UUID;

public interface DeliveryEarningService {

    Page<EarningResponse> getEarningsByPartner(UUID partnerId, Pageable pageable);

    BigDecimal getUnsettledBalance(UUID partnerId);

    void settleEarnings(UUID partnerId);
}
