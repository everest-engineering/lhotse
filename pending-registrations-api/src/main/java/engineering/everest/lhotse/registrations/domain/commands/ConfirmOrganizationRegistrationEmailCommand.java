package engineering.everest.lhotse.registrations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ConfirmOrganizationRegistrationEmailCommand implements UserUniqueEmailValidatableCommand {
    @TargetAggregateIdentifier
    private final UUID registrationConfirmationCode;
    private final UUID organizationId;

    @Override
    public String getEmailAddress() {
        return null;
    }
}
