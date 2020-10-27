package engineering.everest.lhotse.api.rest.requests;

import io.swagger.annotations.ApiModel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ApiModel(description = "Updates non-empty user fields. Missing or blank fields are ignored.")
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class UpdateUserRequest {

    private String displayName;
    private String email;
    private String password; // Don't do this. Example only!

    public UpdateUserRequest() {
    }

    public UpdateUserRequest(String displayName, String email, String password) {
        this.displayName = displayName;
        this.email = email;
        this.password = password;
    }
}
