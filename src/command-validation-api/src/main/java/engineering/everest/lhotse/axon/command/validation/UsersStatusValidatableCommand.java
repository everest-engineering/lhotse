package engineering.everest.lhotse.axon.command.validation;

import java.util.Set;
import java.util.UUID;

public interface UsersStatusValidatableCommand extends ValidatableCommand {

    Set<UUID> getUserIds();
}
