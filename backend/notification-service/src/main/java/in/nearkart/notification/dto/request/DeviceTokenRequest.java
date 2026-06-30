package in.nearkart.notification.dto.request;

import in.nearkart.notification.entity.DevicePlatform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DeviceTokenRequest {
    @NotBlank
    private String token;
    @NotNull
    private DevicePlatform platform;
}
