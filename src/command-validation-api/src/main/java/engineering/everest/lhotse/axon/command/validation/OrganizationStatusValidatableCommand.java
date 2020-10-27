package engineering.everest.lhotse.axon.command.validation;

import java.util.UUID;

public interface OrganizationStatusValidatableCommand extends ValidatableCommand {

    UUID getOrganizationId();
}
