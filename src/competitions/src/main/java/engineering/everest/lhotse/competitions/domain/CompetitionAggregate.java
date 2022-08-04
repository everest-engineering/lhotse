package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.competitions.domain.commands.CountVotesAndDeclareOutcomeCommand;
import engineering.everest.lhotse.competitions.domain.commands.CreateCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.EnterPhotoInCompetitionCommand;
import engineering.everest.lhotse.competitions.domain.commands.VoteForPhotoCommand;
import engineering.everest.lhotse.competitions.domain.events.CompetitionCreatedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedAndWinnersDeclaredEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedWithNoEntriesReceivingVotesEvent;
import engineering.everest.lhotse.competitions.domain.events.CompetitionEndedWithNoEntriesSubmittedEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEnteredInCompetitionEvent;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateMember;
import org.axonframework.spring.stereotype.Aggregate;
import org.springframework.data.util.Pair;

import java.io.Serializable;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.ALREADY_ENTERED_IN_COMPETITION;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_ALREADY_ENDED;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MAX_ENTRIES_REACHED;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MINIMUM_SUBMISSION_PERIOD;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MINIMUM_VOTING_PERIOD;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_MIN_1_VOTE_PER_USER;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_SUBMISSIONS_CLOSED;
import static engineering.everest.lhotse.i18n.MessageKeys.COMPETITION_SUBMISSIONS_NOT_OPEN;
import static engineering.everest.lhotse.i18n.MessageKeys.PHOTO_NOT_ENTERED_IN_COMPETITION;
import static engineering.everest.lhotse.i18n.MessageKeys.SUBMISSIONS_CLOSE_TIMESTAMP_IN_PAST;
import static engineering.everest.lhotse.i18n.MessageKeys.SUBMISSION_BY_NON_PHOTO_OWNER;
import static engineering.everest.lhotse.i18n.MessageKeys.VOTING_ENDED;
import static engineering.everest.lhotse.i18n.MessageKeys.VOTING_PERIOD_NOT_STARTED;
import static java.lang.Integer.MIN_VALUE;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(snapshotTriggerDefinition = "competitionAggregateSnapshotTriggerDefinition")
public class CompetitionAggregate implements Serializable {

    private static final Duration MINIMUM_SUBMISSION_PERIOD = Duration.ofMinutes(5);
    private static final Duration MINIMUM_VOTING_PERIOD = Duration.ofMinutes(5);
    private static final int MIN_ENTRIES_PER_USER = 1;

    @AggregateIdentifier
    private UUID competitionId;
    private Instant submissionsOpenTimestamp;
    private Instant submissionsCloseTimestamp;
    private Instant votingEndsTimestamp;
    private int maxEntriesPerUser;
    private Map<UUID, Integer> numEntriesReceivedPerUser;
    @AggregateMember
    private Map<UUID, CompetitionEntryEntity> submittedPhotos;
    private boolean competitionEnded;

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

    @CommandHandler
    void handle(EnterPhotoInCompetitionCommand command,
                Clock clock,
                AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        validateCompetitionHasNotEnded(axonCommandExecutionExceptionFactory);
        validateRequestingUserIsOwnerOfPhoto(command.getRequestingUserId(), command.getPhotoOwnerUserId(),
            axonCommandExecutionExceptionFactory);
        validatePhotoNotAlreadyEntered(command.getPhotoId(), axonCommandExecutionExceptionFactory);
        validateSubmissionsOpen(axonCommandExecutionExceptionFactory, clock);
        validateMaxEntriesNotExceeded(command.getRequestingUserId(), axonCommandExecutionExceptionFactory);

        apply(new PhotoEnteredInCompetitionEvent(command.getCompetitionId(), command.getPhotoId(), command.getRequestingUserId(),
            command.getPhotoOwnerUserId(), command.getSubmissionNotes()));
    }

    @CommandHandler
    void handle(VoteForPhotoCommand command,
                Clock clock,
                AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        validateCompetitionHasNotEnded(axonCommandExecutionExceptionFactory);
        validateVotingPeriodOpen(axonCommandExecutionExceptionFactory, clock);
        validatePhotoIsEnteredInCompetition(command.getPhotoId(), axonCommandExecutionExceptionFactory);

        submittedPhotos.get(command.getPhotoId()).handle(command, axonCommandExecutionExceptionFactory);
    }

    @CommandHandler
    void handle(CountVotesAndDeclareOutcomeCommand command, AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        validateCompetitionHasNotEnded(axonCommandExecutionExceptionFactory);

        if (submittedPhotos.isEmpty()) {
            apply(new CompetitionEndedWithNoEntriesSubmittedEvent(competitionId));
            return;
        }

        var entriesReceivingMostVotes = findEntriesWithMostVotes();
        if (entriesReceivingMostVotes.isEmpty()) {
            apply(new CompetitionEndedWithNoEntriesReceivingVotesEvent(competitionId));
            return;
        }

        var numVotesReceived = entriesReceivingMostVotes.get(0).getUsersVotedFor().size();
        var winnersToPhotoList = entriesReceivingMostVotes.stream()
            .sorted(comparing(CompetitionEntryEntity::getPhotoId))
            .map(entry -> Pair.of(entry.getSubmittedByUserId(), entry.getPhotoId()))
            .collect(toList());
        apply(new CompetitionEndedAndWinnersDeclaredEvent(competitionId, winnersToPhotoList, numVotesReceived));
    }

    @EventSourcingHandler
    void on(CompetitionCreatedEvent event) {
        competitionId = event.getCompetitionId();
        submissionsOpenTimestamp = event.getSubmissionsOpenTimestamp();
        submissionsCloseTimestamp = event.getSubmissionsCloseTimestamp();
        votingEndsTimestamp = event.getVotingEndsTimestamp();
        maxEntriesPerUser = event.getMaxEntriesPerUser();
        numEntriesReceivedPerUser = new HashMap<>();
        submittedPhotos = new HashMap<>();
        competitionEnded = false;
    }

    @EventSourcingHandler
    void on(PhotoEnteredInCompetitionEvent event) {
        var previousNumEntriesForSubmitter = numEntriesReceivedPerUser.computeIfAbsent(event.getSubmittedByUserId(), (uuid) -> 0);
        numEntriesReceivedPerUser.put(event.getSubmittedByUserId(), previousNumEntriesForSubmitter + 1);
        submittedPhotos.put(event.getPhotoId(), new CompetitionEntryEntity(event.getPhotoId(), event.getSubmittedByUserId()));
    }

    @EventSourcingHandler
    void on(CompetitionEndedEvent event) {
        competitionEnded = true;
    }

    private void validateCompetitionHasNotEnded(AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (competitionEnded) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(COMPETITION_ALREADY_ENDED));
        }
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

    private static void validateSubmissionsCloseTimestampNotInThePast(CreateCompetitionCommand command,
                                                                      Clock clock,
                                                                      AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (command.getSubmissionsCloseTimestamp().isBefore(clock.instant())) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(SUBMISSIONS_CLOSE_TIMESTAMP_IN_PAST));
        }
    }

    private static void validateRequestingUserIsOwnerOfPhoto(UUID requestingUserId,
                                                             UUID ownerUserId,
                                                             AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (!requestingUserId.equals(ownerUserId)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(SUBMISSION_BY_NON_PHOTO_OWNER));
        }
    }

    private void validatePhotoNotAlreadyEntered(UUID photoId, AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (submittedPhotos.containsKey(photoId)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(ALREADY_ENTERED_IN_COMPETITION));
        }
    }

    private void validateSubmissionsOpen(AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory, Clock clock) {
        var now = clock.instant();
        if (now.isBefore(submissionsOpenTimestamp)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(COMPETITION_SUBMISSIONS_NOT_OPEN, submissionsCloseTimestamp));
        }
        if (now.isAfter(submissionsCloseTimestamp)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(COMPETITION_SUBMISSIONS_CLOSED, submissionsCloseTimestamp));
        }
    }

    private void validateVotingPeriodOpen(AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory, Clock clock) {
        var now = clock.instant();
        if (now.isBefore(submissionsCloseTimestamp)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(VOTING_PERIOD_NOT_STARTED, submissionsCloseTimestamp));
        }
        if (now.isAfter(votingEndsTimestamp)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(VOTING_ENDED, votingEndsTimestamp));
        }
    }

    private void validateMaxEntriesNotExceeded(UUID requestingUserId,
                                               AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (numEntriesReceivedPerUser.computeIfAbsent(requestingUserId, (uuid) -> 0) == maxEntriesPerUser) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(COMPETITION_MAX_ENTRIES_REACHED, maxEntriesPerUser));
        }
    }

    private void validatePhotoIsEnteredInCompetition(UUID photoId,
                                                     AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (!submittedPhotos.containsKey(photoId)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(PHOTO_NOT_ENTERED_IN_COMPETITION));
        }
    }

    private List<CompetitionEntryEntity> findEntriesWithMostVotes() {
        var highestVoteCount = submittedPhotos.values().stream()
            .filter(entry -> !entry.getUsersVotedFor().isEmpty())
            .mapToInt(entry -> entry.getUsersVotedFor().size())
            .max()
            .orElse(MIN_VALUE);
        return submittedPhotos.values().stream()
            .filter(entry -> entry.getUsersVotedFor().size() == highestVoteCount)
            .collect(toList());
    }
}
