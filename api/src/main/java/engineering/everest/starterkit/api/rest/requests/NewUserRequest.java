package engineering.everest.starterkit.api.rest.requests;

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
    private String username;

    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(required = true)
    private String password; // MVP only; we need a proper user on-boarding experience for beta.

    @NotBlank
    @Size(max = 100)
    @ApiModelProperty(required = true)
    private String displayName;

}
