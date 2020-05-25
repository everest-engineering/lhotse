package engineering.everest.lhotse.users.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class AdminUserCreatedForNewlyRegisteredOrganizationEvent {
    private UUID userId;
    private UUID organizationId;
    private String userDisplayName;
    private String userEmail;
    private String encodedPassword;
}
