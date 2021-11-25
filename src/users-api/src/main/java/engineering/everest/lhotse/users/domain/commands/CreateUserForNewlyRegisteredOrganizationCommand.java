package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserForNewlyRegisteredOrganizationCommand implements UserUniqueEmailValidatableCommand {
    @TargetAggregateIdentifier
    UUID organizationId;
    @NotNull
    UUID userId;
    @NotBlank
    String userEmail;
    @NotBlank
    String displayName;

    @Override
    public String getEmailAddress() {
        return userEmail;
    }
}
