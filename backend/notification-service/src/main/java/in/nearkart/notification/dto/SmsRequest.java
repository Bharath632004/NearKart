package in.nearkart.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SmsRequest {
    private String userId;

    @NotBlank
    private String to;

    @NotBlank
    private String message;

    private String notificationType;
}
