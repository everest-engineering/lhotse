package engineering.everest.lhotse.competitions.handlers;

import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.persistence.CompetitionsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CompetitionsEventHandlerTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);

    private CompetitionsEventHandler competitionsEventHandler;

    @Mock
    private CompetitionsRepository competitionsRepository;

    @BeforeEach
    void setUp() {
        competitionsEventHandler = new CompetitionsEventHandler(competitionsRepository);
    }

    @Test
    void prepareForReplay_WillClearProjection() {
        competitionsEventHandler.prepareForReplay();

        verify(competitionsRepository).deleteAll();
    }

    @Test
    void onCompetitionCreatedEvent_WillProject() {
        competitionsEventHandler.on(new CompetitionCreatedEvent(USER_ID, COMPETITION_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2));

        verify(competitionsRepository).createCompetition(COMPETITION_ID, "description", SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2);
    }
}
