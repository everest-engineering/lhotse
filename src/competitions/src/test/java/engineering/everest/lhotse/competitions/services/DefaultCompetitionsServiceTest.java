package engineering.everest.lhotse.competitions.services;

import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultCompetitionsServiceTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);

    private DefaultCompetitionsService defaultCompetitionsService;

    @Mock
    private CommandGateway commandGateway;
    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;

    @BeforeEach
    void setUp() {
        defaultCompetitionsService = new DefaultCompetitionsService(commandGateway, randomFieldsGenerator);
    }

    @Test
    void createCompetition_WillDispatch() {
        when(randomFieldsGenerator.genRandomUUID()).thenReturn(COMPETITION_ID);
        defaultCompetitionsService.createCompetition(USER_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP, SUBMISSIONS_CLOSE_TIMESTAMP,
            VOTING_ENDS_TIMESTAMP, 2);

        verify(commandGateway).sendAndWait(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2));
    }
}
