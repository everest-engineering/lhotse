package engineering.everest.lhotse.competitions.handlers;

import engineering.everest.lhotse.competitions.domain.CompetitionWithEntries;
import engineering.everest.lhotse.competitions.domain.queries.CompetitionWithEntriesQuery;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionsQueryHandlerTest {

    private static final UUID COMPETITION_ID = randomUUID();

    private CompetitionsQueryHandler competitionsQueryHandler;

    @Mock
    private CompetitionsReadService competitionsReadService;

    @BeforeEach
    void setUp() {
        competitionsQueryHandler = new CompetitionsQueryHandler(competitionsReadService);
    }

    @Test
    void competitionsWithEntriesQuery_WillDelegate() {
        var expected = mock(CompetitionWithEntries.class);
        when(competitionsReadService.getCompetitionWithEntries(COMPETITION_ID)).thenReturn(expected);

        assertEquals(expected, competitionsQueryHandler.handle(new CompetitionWithEntriesQuery(COMPETITION_ID)));
    }
}
