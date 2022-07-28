package engineering.everest.lhotse.users.services;

import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class DefaultUsersServiceTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();

    @Mock
    private CommandGateway commandGateway;

    private DefaultUsersService defaultUsersService;

    @BeforeEach
    void setUp() {
        defaultUsersService = new DefaultUsersService(commandGateway);
    }

    @Test
    void deleteAndForget_WillSendCommand() {
        defaultUsersService.deleteAndForgetUser(ADMIN_ID, USER_ID, "User requested and we do the right thing");
        verify(commandGateway).sendAndWait(new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, "User requested and we do the right thing"));
    }
}
