package engineering.everest.lhotse.users.domain.commands;

import engineering.everest.lhotse.axon.command.validation.UsersStatusValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import java.util.Set;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PromoteUserToOrganizationAdminCommand implements UsersStatusValidatableCommand {
    @TargetAggregateIdentifier
    private UUID organizationId;
    private UUID promotedUserId;

    @Override
    public Set<UUID> getUserIds() {
        return Set.of(promotedUserId);
    }
}
