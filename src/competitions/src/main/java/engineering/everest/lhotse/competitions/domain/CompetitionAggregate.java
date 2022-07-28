package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MINIMUM_SUBMISSION_PERIOD;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MINIMUM_VOTING_PERIOD;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MIN_1_VOTE_PER_USER;
import static engineering.everest.lhotse.i18n.MessageKeys.SUBMISSIONS_CLOSE_TIMESTAMP_IN_PAST;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(snapshotTriggerDefinition = "competitionAggregateSnapshotTriggerDefinition")
public class CompetitionAggregate implements Serializable {

    private static final Duration MINIMUM_SUBMISSION_PERIOD = Duration.ofHours(1);
    private static final Duration MINIMUM_VOTING_PERIOD = Duration.ofHours(1);
    private static final int MIN_ENTRIES_PER_USER = 1;

    @AggregateIdentifier
    private UUID competitionId;
    private Instant submissionsOpenTimestamp;
    private Instant submissionsCloseTimestamp;
    private Instant votingEndsTimestamp;
    private int maxEntriesPerUser;
    private Map<UUID, Integer> numEntriesReceivedPerUser;

    CompetitionAggregate() {}

    @CommandHandler
    CompetitionAggregate(CreateCompetitionCommand command,
                         Clock clock,
                         AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        validateSubmissionsCloseTimestampNotInThePast(command, clock, axonCommandExecutionExceptionFactory);
        validateSubmissionPeriodEqualOrGreaterThanMinDuration(command, axonCommandExecutionExceptionFactory);
        validateVotingPeriodEqualOrGreaterThanMinDuration(command, axonCommandExecutionExceptionFactory);
        validateMaxVotesPerUserAtLeastOne(command, axonCommandExecutionExceptionFactory);

        apply(new CompetitionCreatedEvent(command.getRequestingUserId(), command.getCompetitionId(), command.getDescription(),
            command.getSubmissionsOpenTimestamp(), command.getSubmissionsCloseTimestamp(), command.getVotingEndsTimestamp(),
            command.getMaxEntriesPerUser()));
    }

    @EventSourcingHandler
    void on(CompetitionCreatedEvent event) {
        competitionId = event.getCompetitionId();
        submissionsOpenTimestamp = event.getSubmissionsOpenTimestamp();
        submissionsCloseTimestamp = event.getSubmissionsCloseTimestamp();
        votingEndsTimestamp = event.getVotingEndsTimestamp();
        maxEntriesPerUser = event.getMaxEntriesPerUser();
        numEntriesReceivedPerUser = new HashMap<>();
    }

    private static void validateMaxVotesPerUserAtLeastOne(CreateCompetitionCommand command,
                                                          AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (command.getMaxEntriesPerUser() < MIN_ENTRIES_PER_USER) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(COMPETITION_MIN_1_VOTE_PER_USER));
        }
    }

    private static void validateVotingPeriodEqualOrGreaterThanMinDuration(CreateCompetitionCommand command,
                                                                          AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        var votingPeriod = Duration.between(command.getSubmissionsCloseTimestamp(), command.getVotingEndsTimestamp());
        if (votingPeriod.minus(MINIMUM_VOTING_PERIOD).isNegative()) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(COMPETITION_MINIMUM_VOTING_PERIOD, MINIMUM_SUBMISSION_PERIOD));
        }
    }

    private static void validateSubmissionPeriodEqualOrGreaterThanMinDuration(CreateCompetitionCommand command,
                                                                              AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        var submissionsOpenDuration = Duration.between(command.getSubmissionsOpenTimestamp(), command.getSubmissionsCloseTimestamp());
        if (submissionsOpenDuration.minus(MINIMUM_SUBMISSION_PERIOD).isNegative()) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(COMPETITION_MINIMUM_SUBMISSION_PERIOD, MINIMUM_SUBMISSION_PERIOD));
        }
    }

    private void validateSubmissionsCloseTimestampNotInThePast(CreateCompetitionCommand command,
                                                               Clock clock,
                                                               AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (command.getSubmissionsCloseTimestamp().isBefore(clock.instant())) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(SUBMISSIONS_CLOSE_TIMESTAMP_IN_PAST));
        }
    }
}
