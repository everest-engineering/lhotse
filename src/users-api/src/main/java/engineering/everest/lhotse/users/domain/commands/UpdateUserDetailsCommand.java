package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.UsersStatusValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserDetailsCommand implements UsersStatusValidatableCommand, EmailAddressValidatableCommand,
        UserUniqueEmailValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private String emailChange;
    private String displayNameChange;

    @NotNull
    private UUID requestingUserId;

    @Override
    public String getEmailAddress() {
        return emailChange;
    }

    @Override
    public Set<UUID> getUserIds() {
        return Set.of(userId, requestingUserId);
    }
}
