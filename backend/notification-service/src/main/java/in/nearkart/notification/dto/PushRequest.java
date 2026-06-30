package in.nearkart.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class PushRequest {
    private String userId;   // send to all tokens of user
    private String fcmToken; // OR single token

    @NotBlank
    private String title;

    @NotBlank
    private String body;

    private Map<String, String> data;
    private String imageUrl;
    private String notificationType;
}
