package in.nearkart.auth.dto.response;

import in.nearkart.auth.entity.Role;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class AuthResponse {

    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UUID userId;
    private String fullName;
    private String phone;
    private Role role;
    private Boolean isVerified;
}
