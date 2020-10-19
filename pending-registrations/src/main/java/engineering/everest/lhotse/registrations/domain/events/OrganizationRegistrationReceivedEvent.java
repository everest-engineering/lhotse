package engineering.everest.lhotse.registrations.domain.events;

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
public class OrganizationRegistrationReceivedEvent {
    private UUID organizationId;
    @EncryptionKeyIdentifier
    private UUID registeringUserId;
    private UUID registrationConfirmationCode;
    @EncryptedField
    private String registeringContactEmail;
    private String registeringUserEncodedPassword;
    private String organizationName;
    private String websiteUrl;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    @EncryptedField
    private String contactName;
    @EncryptedField
    private String contactPhoneNumber;
}
