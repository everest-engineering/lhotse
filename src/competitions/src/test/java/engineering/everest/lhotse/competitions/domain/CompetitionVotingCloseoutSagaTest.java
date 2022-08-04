package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.competitions.domain.commands.CountVotesAndDeclareOutcomeCommand;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionVotingPeriodEndedEvent;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;

@ExtendWith(MockitoExtension.class)
class CompetitionVotingCloseoutSagaTest {

    private static final UUID ADMIN_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final Instant VOTING_ENDS_TIMESTAMP = Instant.ofEpochMilli(789);

    private static final CompetitionVotingPeriodEndedEvent SCHEDULED_VOTING_PERIOD_ENDED_EVENT =
        new CompetitionVotingPeriodEndedEvent(COMPETITION_ID, VOTING_ENDS_TIMESTAMP);
    private static final CompetitionCreatedEvent COMPETITION_CREATED_EVENT =
        new CompetitionCreatedEvent(ADMIN_ID, COMPETITION_ID, "description", Instant.ofEpochMilli(123),
            Instant.ofEpochMilli(456), VOTING_ENDS_TIMESTAMP, 1);
    private static final CompetitionEndedEvent COMPETITION_ENDED_EVENT = new CompetitionEndedEvent(COMPETITION_ID);

    private SagaTestFixture<CompetitionVotingCloseoutSaga> testFixture;

    @Autowired
    private CommandGateway commandGateway;

    @BeforeEach
    void setUp() {
        testFixture = new SagaTestFixture<>(CompetitionVotingCloseoutSaga.class);
    }

    @Test
    void onCompetitionCreated_WillScheduleVotingPeriodEndedEvent() {
        testFixture.givenNoPriorActivity()
            .whenAggregate(COMPETITION_ID.toString())
            .publishes(COMPETITION_CREATED_EVENT)
            .expectNoDispatchedCommands()
            .expectScheduledEvent(VOTING_ENDS_TIMESTAMP, SCHEDULED_VOTING_PERIOD_ENDED_EVENT);
    }

    @Test
    void onVotingPeriodEndedEvent_WillDispatchCommandToCountVotes() {
        testFixture.givenAPublished(COMPETITION_CREATED_EVENT)
            .whenPublishingA(SCHEDULED_VOTING_PERIOD_ENDED_EVENT)
            .expectDispatchedCommands(new CountVotesAndDeclareOutcomeCommand(COMPETITION_ID));
    }

    @Test
    void onCompetitionEndedEvent_WillEndSagaLifecycle() {
        testFixture.givenAPublished(COMPETITION_CREATED_EVENT)
            .andThenAPublished(SCHEDULED_VOTING_PERIOD_ENDED_EVENT)
            .whenAggregate(COMPETITION_ID.toString())
            .publishes(COMPETITION_ENDED_EVENT)
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);
    }
}
