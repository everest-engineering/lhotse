package engineering.everest.lhotse.api.rest.requests;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAndForgetUserRequest {
    @NotBlank
    @Schema(required = true)
    private String requestReason;
}
