package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.UsersStatusValidatableCommand;
import engineering.everest.lhotse.common.domain.Role;
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
public class AddUserRolesCommand implements UsersStatusValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private Set<Role> roles;

    @NotNull
    private UUID requestingUserId;

    @Override
    public Set<UUID> getUserIds() {
        return Set.of(userId, requestingUserId);
    }
}
