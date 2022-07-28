package engineering.everest.lhotse.users.services;

import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class DefaultUsersService implements UsersService {

    private final CommandGateway commandGateway;

    public DefaultUsersService(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Override
    public void deleteAndForgetUser(UUID requestingUserId, UUID userIdToDelete, String requestReason) {
        commandGateway.sendAndWait(new DeleteAndForgetUserCommand(userIdToDelete, requestingUserId, requestReason));
    }
}
