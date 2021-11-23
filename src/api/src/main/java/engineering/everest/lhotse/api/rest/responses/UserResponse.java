package engineering.everest.lhotse.api.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private UUID organizationId;
    private String username;
    private String displayName;
    private String email;
    private boolean disabled;
}
