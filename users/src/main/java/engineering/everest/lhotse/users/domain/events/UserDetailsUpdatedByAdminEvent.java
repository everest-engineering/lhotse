package engineering.everest.lhotse.users.domain.events;

import engineering.everest.starterkit.axon.cryptoshredding.annotations.EncryptedField;
import engineering.everest.starterkit.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
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
    @EncryptionKeyIdentifier
    private UUID userId;
    private UUID organizationId;
    @EncryptedField
    private String displayNameChange;
    @EncryptedField
    private String emailChange;
    private String encodedPasswordChange;
    private UUID adminId;
}
