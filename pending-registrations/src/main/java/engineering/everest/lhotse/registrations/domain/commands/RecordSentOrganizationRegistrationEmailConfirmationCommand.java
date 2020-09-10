package engineering.everest.lhotse.registrations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import engineering.everest.starterkit.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@AllArgsConstructor
public class RecordSentOrganizationRegistrationEmailConfirmationCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private final UUID registrationConfirmationCode;
    private final UUID organizationId;
    private final String registeringContactEmail;
    private final String organizationName;
    @EncryptionKeyIdentifier
    private final UUID registeringUserId;
}
