package engineering.everest.lhotse.competitions.persistence;

import engineering.everest.lhotse.competitions.persistence.config.TestCompetitionsJpaConfig;
import engineering.everest.lhotse.competitions.services.DefaultCompetitionsWriteService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.UUID;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;

@AutoConfigureEmbeddedDatabase(refresh = AFTER_EACH_TEST_METHOD, type = POSTGRES)
@DataJpaTest
@EnableAutoConfiguration
@ComponentScan(basePackages = "engineering.everest.lhotse.competitions")
@ContextConfiguration(classes = { TestCompetitionsJpaConfig.class })
@Execution(SAME_THREAD)
public class CompetitionsWriteServiceIntegrationTest {

    private static final UUID COMPETITION_ID = randomUUID();
    private static final UUID SECOND_COMPETITION_ID = randomUUID();
    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final String DESCRIPTION = "competition 1";
    private static final Instant SUBMISSIONS_OPEN_TIMESTAMP = Instant.ofEpochMilli(123);
    private static final Instant SUBMISSIONS_CLOSE_TIMESTAMP = Instant.ofEpochMilli(456);
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);
    private static final Instant ENTRY_TIMESTAMP = Instant.ofEpochMilli(100);

    @Autowired
    private DefaultCompetitionsWriteService competitionsWriteService;

    @Autowired
    private CompetitionEntriesRepository competitionEntriesRepository;

    @Autowired
    private CompetitionsRepository competitionsRepository;

    @Test
    void createCompetition_WillPersistCompetition() {
        competitionsWriteService.createCompetition(COMPETITION_ID, DESCRIPTION, SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2);

        assertTrue(competitionsRepository.findById(COMPETITION_ID).isPresent());
    }

    @Test
    void createCompetitionEntry_WillPersistCompetitionEntry() {
        competitionsWriteService.createCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);

        assertTrue(competitionEntriesRepository.findById(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID)).isPresent());
    }

    @Test
    void incrementVotesReceived_WillPersistTheIncrementedVotes() {
        competitionsWriteService.createCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);

        competitionsWriteService.incrementVotesReceived(COMPETITION_ID, PHOTO_ID);

        var competitionEntry = competitionEntriesRepository.findById(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID));
        assertTrue(competitionEntry.isPresent());
        assertEquals(1, competitionEntry.get().getVotesReceived());
    }

    @Test
    void setCompetitionWinner_WillPersistCompetitionWinner() {
        competitionsWriteService.createCompetitionEntry(COMPETITION_ID, PHOTO_ID, USER_ID, ENTRY_TIMESTAMP);

        competitionsWriteService.setCompetitionWinner(COMPETITION_ID, PHOTO_ID);

        var competitionEntry = competitionEntriesRepository.findById(new CompetitionEntryId(COMPETITION_ID, PHOTO_ID));
        assertTrue(competitionEntry.isPresent());
        assertTrue(competitionEntry.get().isWinner());
    }

    @Test
    void deleteAll_WillDeleteUpAllCompetitionRecords() {
        competitionsWriteService.createCompetition(COMPETITION_ID, DESCRIPTION, SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2);
        competitionsWriteService.createCompetition(SECOND_COMPETITION_ID, DESCRIPTION, SUBMISSIONS_OPEN_TIMESTAMP,
            SUBMISSIONS_CLOSE_TIMESTAMP, VOTING_ENDS_TIMESTAMP, 2);

        competitionsWriteService.deleteAll();

        assertTrue(competitionsRepository.findAll().isEmpty());
    }
}
