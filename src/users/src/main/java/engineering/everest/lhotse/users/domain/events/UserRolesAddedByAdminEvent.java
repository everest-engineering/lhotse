package engineering.everest.lhotse.users.domain.events;

import engineering.everest.lhotse.axon.common.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class UserRolesAddedByAdminEvent {
    private UUID userId;
    private Set<Role> roles;

    @NotNull
    private UUID requestingUserId;
}
