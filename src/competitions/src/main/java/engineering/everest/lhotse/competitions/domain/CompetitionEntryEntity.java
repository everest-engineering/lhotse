package engineering.everest.lhotse.competitions.domain;

import engineering.everest.lhotse.competitions.domain.commands.VoteForPhotoCommand;
import engineering.everest.lhotse.competitions.domain.events.PhotoEntryReceivedVoteEvent;
import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.EntityId;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.ALREADY_VOTED_FOR_THIS_ENTRY;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@NoArgsConstructor
@EqualsAndHashCode
@Getter
public class CompetitionEntryEntity implements Serializable {

    @EntityId
    private UUID photoId;
    private UUID submittedByUserId;
    private Set<UUID> usersVotedFor;

    CompetitionEntryEntity(UUID photoId, UUID submittedByUserId) {
        this.photoId = photoId;
        this.submittedByUserId = submittedByUserId;
        this.usersVotedFor = new HashSet<>();
    }

    // Not annotated with @CommandHandler on purpose. Some validation occurs in aggregate root.
    void handle(VoteForPhotoCommand command) {
        validateUserHasNotVotedForPhotoBefore(command.getRequestingUserId());

        apply(new PhotoEntryReceivedVoteEvent(command.getCompetitionId(), photoId, command.getRequestingUserId()));
    }

    @EventSourcingHandler
    void on(PhotoEntryReceivedVoteEvent event) {
        if (event.getPhotoId().equals(photoId)) {
            usersVotedFor.add(event.getVotingUserId());
        }
    }

    private void validateUserHasNotVotedForPhotoBefore(UUID requestingUserId) {
        if (usersVotedFor.contains(requestingUserId)) {
            throwWrappedInCommandExecutionException(new TranslatableIllegalStateException(ALREADY_VOTED_FOR_THIS_ENTRY));
        }
    }

    private static void throwWrappedInCommandExecutionException(TranslatableException translatableException) {
        throw new CommandExecutionException(translatableException.getMessage(), null, translatableException);
    }
}
