package engineering.everest.lhotse.registrations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@AllArgsConstructor
public class ConfirmOrganizationRegistrationEmailCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private final UUID registrationConfirmationCode;
    private final UUID organizationId;
}
