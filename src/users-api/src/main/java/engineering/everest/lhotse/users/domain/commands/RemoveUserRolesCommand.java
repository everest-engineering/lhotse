package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import engineering.everest.lhotse.axon.common.domain.Role;
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
public class RemoveUserRolesCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private Set<Role> roles;

    @NotNull
    private UUID requestingUserId;
}
