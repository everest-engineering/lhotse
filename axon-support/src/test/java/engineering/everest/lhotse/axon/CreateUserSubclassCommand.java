package engineering.everest.lhotse.axon;

import engineering.everest.lhotse.axon.command.validation.UsersBelongToOrganizationValidatableCommand;
import engineering.everest.lhotse.users.domain.commands.CreateUserCommand;

import java.util.Set;
import java.util.UUID;

import static java.util.Collections.singleton;

public class CreateUserSubclassCommand extends CreateUserCommand implements UsersBelongToOrganizationValidatableCommand {

    @Override
    public Set<UUID> getUserIds() {
        return singleton(getUserId());
    }
}
