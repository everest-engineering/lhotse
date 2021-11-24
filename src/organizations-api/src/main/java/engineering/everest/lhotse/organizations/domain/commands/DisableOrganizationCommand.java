package engineering.everest.lhotse.organizations.domain.commands;

import engineering.everest.lhotse.axon.command.validation.OrganizationStatusValidatableCommand;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.NotNull;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DisableOrganizationCommand implements OrganizationStatusValidatableCommand {
    @TargetAggregateIdentifier
    private UUID organizationId;

    @NotNull
    private UUID requestingUserId;
}
