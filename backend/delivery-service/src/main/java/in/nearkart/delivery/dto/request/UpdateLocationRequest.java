package in.nearkart.delivery.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateLocationRequest {

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    private Double speedKmph;
    private Double headingDegrees;
}
