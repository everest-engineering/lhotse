package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@AllArgsConstructor
public class CreateUserForNewlyRegisteredOrganizationCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    UUID organizationId;
    @NotNull
    UUID userId;
    @NotBlank
    String userEmail;
    @NotBlank
    String displayName;
}
