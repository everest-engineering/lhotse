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
    @EncryptedField
    private String websiteUrl;
    @EncryptedField
    private String street;
    @EncryptedField
    private String city;
    @EncryptedField
    private String state;
    @EncryptedField
    private String country;
    @EncryptedField
    private String postalCode;
    @EncryptedField
    private String contactName;
    @EncryptedField
    private String contactPhoneNumber;
}
