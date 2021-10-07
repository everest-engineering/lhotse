package engineering.everest.lhotse.users.domain.events;

import engineering.everest.axon.cryptoshredding.annotations.EncryptedField;
import engineering.everest.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class UserCreatedForNewlyRegisteredOrganizationEvent {
    @EncryptionKeyIdentifier
    private UUID organizationId;
    private UUID userId;
    @EncryptedField
    private String userDisplayName;
    @EncryptedField
    private String userEmail;
    private String encodedPassword;
}
