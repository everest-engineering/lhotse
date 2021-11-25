package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.OrganizationStatusValidatableCommand;
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
public class CreateOrganizationUserCommand implements EmailAddressValidatableCommand, UserUniqueEmailValidatableCommand,
        OrganizationStatusValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    @NotNull
    private UUID organizationId;
    @NotNull
    private UUID requestingUserId;
    @NotNull
    private String username;
    @NotBlank
    private String userDisplayName;

    @Override
    public String getEmailAddress() {
        return username;
    }
}
