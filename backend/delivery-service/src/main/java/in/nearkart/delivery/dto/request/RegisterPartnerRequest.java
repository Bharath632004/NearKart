package in.nearkart.delivery.dto.request;

import in.nearkart.delivery.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterPartnerRequest {

    @NotBlank
    private String fullName;

    @NotBlank
    private String phone;

    private String email;

    @NotBlank
    private String aadhaarNumber;

    @NotBlank
    private String panNumber;

    @NotBlank
    private String vehicleNumber;

    @NotNull
    private VehicleType vehicleType;
}
