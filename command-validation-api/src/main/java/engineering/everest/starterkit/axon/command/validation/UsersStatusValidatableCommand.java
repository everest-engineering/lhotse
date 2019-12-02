package engineering.everest.starterkit.axon.command.validation;

import java.util.Set;
import java.util.UUID;

public interface UsersStatusValidatableCommand extends ValidatableCommand {

    Set<UUID> getUserIds();
}
