package engineering.everest.starterkit.users.domain.commands;

import engineering.everest.starterkit.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.OrganizationStatusValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.UserUniqueEmailValidatableCommand;
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
public class CreateUserCommand implements EmailAddressValidatableCommand, UserUniqueEmailValidatableCommand,
        OrganizationStatusValidatableCommand {

    @TargetAggregateIdentifier
    private UUID userId;
    @NotNull
    private UUID organizationId;
    @NotNull
    private UUID requestingUserId;
    @NotNull
    private String username;
    @NotNull
    private String encodedPassword;
    @NotBlank
    private String userDisplayName;

    @Override
    public String getEmailAddress() {
        return username;
    }

}
