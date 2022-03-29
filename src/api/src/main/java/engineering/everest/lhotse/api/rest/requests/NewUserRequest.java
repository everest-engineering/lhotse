package engineering.everest.lhotse.api.rest.requests;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NewUserRequest {

    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(required = true)
    private String emailAddress;

    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(required = true)
    private String displayName;

}
