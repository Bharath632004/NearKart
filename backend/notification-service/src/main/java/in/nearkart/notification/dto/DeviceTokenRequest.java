package in.nearkart.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DeviceTokenRequest {
    @NotBlank
    private String userId;

    @NotBlank
    private String fcmToken;

    private String deviceType;
}
