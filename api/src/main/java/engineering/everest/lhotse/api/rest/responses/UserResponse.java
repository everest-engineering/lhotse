package engineering.everest.lhotse.api.rest.responses;

import engineering.everest.lhotse.axon.common.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
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
    private Set<Role> roles;
}
