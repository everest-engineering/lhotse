package engineering.everest.lhotse.registrations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.UUID;

@Data
@AllArgsConstructor
public class CancelConfirmedRegistrationUserEmailAlreadyInUseCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private UUID registrationConfirmationCode;
    private UUID organizationId;
    private UUID registeringUserId;
    private String registeringUserEmail;
}
