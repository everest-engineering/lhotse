package engineering.everest.lhotse.api.rest.requests;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel(description = "Updates non-empty user fields. Missing or blank fields are ignored.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {

    private String displayName;
    private String email;
}
