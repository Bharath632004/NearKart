package in.nearkart.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class EmailRequest {

    private String userId;

    @NotBlank @Email
    private String to;

    @NotBlank
    private String subject;

    // Either plain body OR template
    private String body;
    private String templateName;
    private Map<String, Object> templateVariables;

    private String notificationType;
}
