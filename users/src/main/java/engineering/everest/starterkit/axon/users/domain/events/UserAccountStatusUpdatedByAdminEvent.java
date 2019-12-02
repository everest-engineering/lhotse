package engineering.everest.starterkit.axon.users.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class UserAccountStatusUpdatedByAdminEvent {

    private UUID disabledUserId;
    private UUID userOrganizationId;
    private UUID adminId;
    private boolean disabled;
}
