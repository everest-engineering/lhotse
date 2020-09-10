package engineering.everest.lhotse.api.rest.requests;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeleteAndForgetUserRequest {

    @NotBlank
    @ApiModelProperty(required = true)
    private String requestReason;
}
