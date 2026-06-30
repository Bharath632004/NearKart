package in.nearkart.delivery.dto.request;

import in.nearkart.delivery.entity.AssignmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateAssignmentStatusRequest {

    @NotNull
    private AssignmentStatus status;

    private String failureReason;
}
