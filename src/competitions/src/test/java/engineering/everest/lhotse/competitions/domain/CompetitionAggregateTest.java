package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.competitions.domain.commands.EnterPhotoInCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.VoteForPhotoCommand;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEnteredInCompetitionEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEntryReceivedVoteEvent;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompetitionAggregateTest {

    private static final Instant FIXED_INSTANT = Instant.ofEpochMilli(1658900761278L);
    private static final UUID USER_ID = randomUUID();
    private static final UUID COMPETITION_ID = randomUUID();
    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID SUBMITTER_ID = randomUUID();
    private static final Instant SUBMISSIONS_OPEN = FIXED_INSTANT.plus(Duration.ofDays(1));
    private static final Instant SUBMISSIONS_CLOSE = FIXED_INSTANT.plus(Duration.ofDays(2));
    private static final Instant VOTING_ENDS = FIXED_INSTANT.plus(Duration.ofDays(3));
    private static final String SUBMISSION_NOTES = "I took this while hiking";
    private static final PhotoEnteredInCompetitionEvent PHOTO_ACCEPTED_INTO_COMPETITION_EVENT =
        new PhotoEnteredInCompetitionEvent(COMPETITION_ID, PHOTO_ID, SUBMITTER_ID, SUBMITTER_ID, SUBMISSION_NOTES);
    private static final CompetitionCreatedEvent COMPETITION_CREATED_EVENT =
        new CompetitionCreatedEvent(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_CLOSE, VOTING_ENDS, 1);
    private static final PhotoEntryReceivedVoteEvent PHOTO_ENTRY_RECEIVED_VOTE_EVENT =
        new PhotoEntryReceivedVoteEvent(COMPETITION_ID, PHOTO_ID, USER_ID);

    private FixtureConfiguration<CompetitionAggregate> testFixture;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();

        testFixture = new AggregateTestFixture<>(CompetitionAggregate.class)
            .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor())
            .registerInjectableResource(axonCommandExecutionExceptionFactory)
            .registerInjectableResource(clock);

        lenient().when(clock.instant()).thenReturn(FIXED_INSTANT);
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var aggregateAnnotation = CompetitionAggregate.class.getAnnotation(Aggregate.class);
        assertEquals("competitionAggregateSnapshotTriggerDefinition", aggregateAnnotation.snapshotTriggerDefinition());
    }

    @Test
    void emits_WhenCompetitionTemporallySane() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, SUBMISSIONS_CLOSE, VOTING_ENDS, 1))
            .expectEvents(COMPETITION_CREATED_EVENT);
    }

    @Test
    void rejects_WhenSubmissionsOpenAfterSubmissionsClose() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_CLOSE, SUBMISSIONS_OPEN, VOTING_ENDS, 1))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MINIMUM_SUBMISSION_PERIOD");
    }

    @Test
    void rejects_WhenVotingEndsBeforeSubmissionsClose() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", SUBMISSIONS_OPEN, VOTING_ENDS, SUBMISSIONS_CLOSE, 1))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MINIMUM_VOTING_PERIOD");
    }

    @Test
    void rejects_WhenSubmissionsCloseInThePast() {
        testFixture.givenNoPriorActivity()
            .when(new CreateCompetitionCommand(USER_ID, COMPETITION_ID, "", FIXED_INSTANT.minus(Duration.ofDays(3)),
                FIXED_INSTANT.minus(Duration.ofSeconds(1)), VOTING_ENDS, 1))
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

    @Test
    void emits_WhenPhotoEnteredDuringSubmissionPeriod() {
        when(clock.instant()).thenReturn(SUBMISSIONS_OPEN.plus(Duration.ofHours(1)));

        testFixture.given(COMPETITION_CREATED_EVENT)
            .when(new EnterPhotoInCompetitionCommand(COMPETITION_ID, PHOTO_ID, SUBMITTER_ID, SUBMITTER_ID, SUBMISSION_NOTES))
            .expectEvents(PHOTO_ACCEPTED_INTO_COMPETITION_EVENT);
    }

    @Test
    void rejects_WhenPhotoAlreadyEnteredInCompetition() {
        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT)
            .when(new EnterPhotoInCompetitionCommand(COMPETITION_ID, PHOTO_ID, SUBMITTER_ID, SUBMITTER_ID, SUBMISSION_NOTES))
            .expectNoEvents()
            .expectExceptionMessage("ALREADY_ENTERED_IN_COMPETITION");
    }

    @Test
    void rejects_WhenPhotoEnteredBeforeSubmissionsOpen() {
        when(clock.instant()).thenReturn(SUBMISSIONS_OPEN.minus(Duration.ofSeconds(1)));

        testFixture.given(COMPETITION_CREATED_EVENT)
            .when(new EnterPhotoInCompetitionCommand(COMPETITION_ID, randomUUID(), SUBMITTER_ID, SUBMITTER_ID, SUBMISSION_NOTES))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_SUBMISSIONS_NOT_OPEN");
    }

    @Test
    void rejects_WhenPhotoEnteredAfterSubmissionsClose() {
        when(clock.instant()).thenReturn(SUBMISSIONS_CLOSE.plus(Duration.ofSeconds(1)));

        testFixture.given(COMPETITION_CREATED_EVENT)
            .when(new EnterPhotoInCompetitionCommand(COMPETITION_ID, randomUUID(), SUBMITTER_ID, SUBMITTER_ID, SUBMISSION_NOTES))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_SUBMISSIONS_CLOSED");
    }

    @Test
    void rejects_WhenMaxEntriesPerPersonReached() {
        when(clock.instant()).thenReturn(SUBMISSIONS_OPEN.plus(Duration.ofHours(1)));

        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT)
            .when(new EnterPhotoInCompetitionCommand(COMPETITION_ID, randomUUID(), SUBMITTER_ID, SUBMITTER_ID, SUBMISSION_NOTES))
            .expectNoEvents()
            .expectExceptionMessage("COMPETITION_MAX_ENTRIES_REACHED");
    }

    @Test
    void rejects_WhenSubmitterIsNotOwnerOfPhoto() {
        testFixture.given(COMPETITION_CREATED_EVENT)
            .when(new EnterPhotoInCompetitionCommand(COMPETITION_ID, PHOTO_ID, SUBMITTER_ID, randomUUID(), SUBMISSION_NOTES))
            .expectNoEvents()
            .expectExceptionMessage("SUBMISSION_BY_NON_PHOTO_OWNER");
    }

    @Test
    void emits_WhenPhotoReceivesVote() {
        when(clock.instant()).thenReturn(SUBMISSIONS_CLOSE);

        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT)
            .when(new VoteForPhotoCommand(COMPETITION_ID, PHOTO_ID, USER_ID))
            .expectEvents(PHOTO_ENTRY_RECEIVED_VOTE_EVENT);
    }

    @Test
    void rejects_WhenPhotoReceivesVoteAndVotingPeriodNotStarted() {
        when(clock.instant()).thenReturn(SUBMISSIONS_CLOSE.minus(Duration.ofSeconds(1)));

        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT)
            .when(new VoteForPhotoCommand(COMPETITION_ID, PHOTO_ID, USER_ID))
            .expectNoEvents()
            .expectExceptionMessage("VOTING_PERIOD_NOT_STARTED");
    }

    @Test
    void rejects_WhenPhotoReceivesVoteAndVotingPeriodEnded() {
        when(clock.instant()).thenReturn(VOTING_ENDS.plus(Duration.ofSeconds(1)));

        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT)
            .when(new VoteForPhotoCommand(COMPETITION_ID, PHOTO_ID, USER_ID))
            .expectNoEvents()
            .expectExceptionMessage("VOTING_ENDED");
    }

    @Test
    void rejects_WhenUserHasVotedForPhotoBefore() {
        when(clock.instant()).thenReturn(SUBMISSIONS_CLOSE);

        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT, PHOTO_ENTRY_RECEIVED_VOTE_EVENT)
            .when(new VoteForPhotoCommand(COMPETITION_ID, PHOTO_ID, USER_ID))
            .expectNoEvents()
            .expectExceptionMessage("ALREADY_VOTED_FOR_THIS_ENTRY");
    }

    @Test
    void rejects_WhenPhotoNotPartOfCompetition() {
        when(clock.instant()).thenReturn(SUBMISSIONS_CLOSE);

        testFixture.given(COMPETITION_CREATED_EVENT, PHOTO_ACCEPTED_INTO_COMPETITION_EVENT)
            .when(new VoteForPhotoCommand(COMPETITION_ID, randomUUID(), USER_ID))
            .expectNoEvents()
            .expectExceptionMessage("PHOTO_NOT_ENTERED_IN_COMPETITION");
    }
}
