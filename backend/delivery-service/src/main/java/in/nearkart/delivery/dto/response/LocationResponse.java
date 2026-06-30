package in.nearkart.delivery.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class LocationResponse {
    private UUID partnerId;
    private UUID assignmentId;
    private Double latitude;
    private Double longitude;
    private Double speedKmph;
    private LocalDateTime recordedAt;
}
