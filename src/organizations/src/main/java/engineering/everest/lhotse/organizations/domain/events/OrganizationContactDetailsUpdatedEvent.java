package engineering.everest.lhotse.organizations.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class OrganizationContactDetailsUpdatedEvent {
    private UUID organizationId;
    private String contactName;
    private String phoneNumber;
    private String emailAddress;
    private String websiteUrl;
    private UUID updatingUserId;
}
