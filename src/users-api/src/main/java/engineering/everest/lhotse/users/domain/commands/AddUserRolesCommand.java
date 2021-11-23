package engineering.everest.lhotse.users.domain.commands;

import java.util.Set;
import java.util.UUID;

import javax.validation.constraints.NotNull;

import engineering.everest.lhotse.axon.command.validation.ValidatableCommand;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import engineering.everest.lhotse.axon.common.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddUserRolesCommand implements ValidatableCommand {
    @TargetAggregateIdentifier
    private UUID userId;
    private Set<Role> roles;

    @NotNull
    private UUID requestingUserId;
}
