package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CompetitionAggregateTest {

    private static final Instant FIXED_INSTANT = Instant.ofEpochMilli(1658900761278L);
    private static final Clock CLOCK = Clock.fixed(FIXED_INSTANT, ZoneId.systemDefault());
    private static final UUID USER_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN = FIXED_INSTANT.plus(Duration.ofDays(1));
    private static final Instant SUBMISSIONS_CLOSE = FIXED_INSTANT.plus(Duration.ofDays(2));
    private static final Instant VOTING_ENDS = FIXED_INSTANT.plus(Duration.ofDays(3));
    private static final CompetitionCreatedEvent COMPETITION_CREATED_EVENT =
        new CompetitionCreatedEvent(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_CLOSE, VOTING_ENDS, 3);

    private FixtureConfiguration<CompetitionAggregate> testFixture;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @BeforeEach
    void setUp() {
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();

        testFixture = new AggregateTestFixture<>(CompetitionAggregate.class)
            .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor())
            .registerInjectableResource(axonCommandExecutionExceptionFactory)
            .registerInjectableResource(CLOCK);
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var aggregateAnnotation = CompetitionAggregate.class.getAnnotation(Aggregate.class);
        assertEquals("competitionAggregateSnapshotTriggerDefinition", aggregateAnnotation.snapshotTriggerDefinition());
    }

    @Test
    void emits_WhenCompetitionTemporallySane() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_CLOSE, VOTING_ENDS, 3))
            .expectEvents(COMPETITION_CREATED_EVENT);
    }

    @Test
    void rejects_WhenSubmissionsOpenAfterSubmissionsClose() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_CLOSE, SUBMISSIONS_OPEN, VOTING_ENDS, 3))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MINIMUM_SUBMISSION_PERIOD");
    }

    @Test
    void rejects_WhenVotingEndsBeforeSubmissionsClose() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, VOTING_ENDS, SUBMISSIONS_CLOSE, 3))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MINIMUM_VOTING_PERIOD");
    }

    @Test
    void rejects_WhenSubmissionsCloseInThePast() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", FIXED_INSTANT.minus(Duration.ofDays(3)),
                FIXED_INSTANT.minus(Duration.ofSeconds(1)), VOTING_ENDS, 3))
            .expectNoEvents()
            .expectExceptionMessage("SUBMISSIONS_CLOSE_TIMESTAMP_IN_PAST");
    }

    @Test
    void rejects_WhenSubmissionPeriodTooShort() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_OPEN.plus(Duration.ofSeconds(10)),
                VOTING_ENDS, 3))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MINIMUM_SUBMISSION_PERIOD");
    }

    @Test
    void rejects_WhenVotingPeriodTooShort() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_CLOSE,
                SUBMISSIONS_CLOSE.plus(Duration.ofSeconds(10)), 3))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MINIMUM_VOTING_PERIOD");
    }

    @Test
    void rejects_WhenNumberOfEntriesPerUserLessThanOne() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_CLOSE, VOTING_ENDS, 0))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MIN_1_VOTE_PER_USER");
    }
}
