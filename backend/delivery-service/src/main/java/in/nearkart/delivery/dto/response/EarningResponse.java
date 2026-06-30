package in.nearkart.delivery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EarningResponse {
    private UUID id;
    private UUID partnerId;
    private UUID assignmentId;
    private BigDecimal amount;
    private String description;
    private boolean settled;
    private LocalDateTime createdAt;
}
