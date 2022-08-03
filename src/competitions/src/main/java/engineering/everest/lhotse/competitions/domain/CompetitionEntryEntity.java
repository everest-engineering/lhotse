package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.competitions.domain.commands.VoteForPhotoCommand;
import engineering.everest.lhotse.competitions.domain.events.PhotoEnteredInCompetitionEvent;
import engineering.everest.lhotse.competitions.domain.events.PhotoEntryReceivedVoteEvent;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.ALREADY_VOTED_FOR_THIS_ENTRY;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@NoArgsConstructor
@EqualsAndHashCode
public class CompetitionEntryEntity {

    @EntityId
    private UUID photoId;
    private Set<UUID> usersVotedFor;

    // Not annotated with @CommandHandler on purpose. Some validation occurs in aggregate root.
    void handle(VoteForPhotoCommand command,
                AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        validateUserHasNotVotedForPhotoBefore(command.getRequestingUserId(), axonCommandExecutionExceptionFactory);

        apply(new PhotoEntryReceivedVoteEvent(command.getCompetitionId(), photoId, command.getRequestingUserId()));
    }

    @EventSourcingHandler
    void on(PhotoEnteredInCompetitionEvent event) {
        photoId = event.getPhotoId();
        usersVotedFor = new HashSet<>();
    }

    @EventSourcingHandler
    void on(PhotoEntryReceivedVoteEvent event) {
        usersVotedFor.add(event.getVotingUserId());
    }

    private void validateUserHasNotVotedForPhotoBefore(UUID requestingUserId,
                                                       AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (usersVotedFor.contains(requestingUserId)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(ALREADY_VOTED_FOR_THIS_ENTRY));
        }
    }
}
