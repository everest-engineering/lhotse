package engineering.everest.starterkit.users.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class UserDetailsUpdatedByAdminEvent {

    private UUID userId;
    private UUID organizationId;
    private String displayNameChange;
    private String emailChange;
    private String encodedPasswordChange;
    private UUID adminId;
}
