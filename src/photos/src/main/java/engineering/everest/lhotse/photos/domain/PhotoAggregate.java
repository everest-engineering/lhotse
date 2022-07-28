package engineering.everest.lhotse.photos.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.photos.domain.commands.DeletePhotoForDeletedUserCommand;
import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import engineering.everest.lhotse.photos.domain.events.PhotoDeletedAsPartOfUserDeletionEvent;
import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.DELETED_PHOTO_OWNER_MISMATCH;
import static org.axonframework.modelling.command.AggregateLifecycle.apply;
import static org.axonframework.modelling.command.AggregateLifecycle.markDeleted;

@Aggregate(snapshotTriggerDefinition = "photoAggregateSnapshotTriggerDefinition")
public class PhotoAggregate implements Serializable {

    @AggregateIdentifier
    private UUID photoId;
    private UUID backingFileId;
    private UUID ownerUserId;

    PhotoAggregate() {}

    @CommandHandler
    PhotoAggregate(RegisterUploadedPhotoCommand command) {
        apply(new PhotoUploadedEvent(command.getPhotoId(), command.getOwningUserId(), command.getBackingFileId(), command.getFilename()));
    }

    @CommandHandler
    void handle(DeletePhotoForDeletedUserCommand command, AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        validatePhotoBelongsToDeletedUser(command, axonCommandExecutionExceptionFactory);

        apply(new PhotoDeletedAsPartOfUserDeletionEvent(photoId, backingFileId, command.getDeletedUserId()));
    }

    @EventSourcingHandler
    void on(PhotoUploadedEvent event) {
        photoId = event.getPhotoId();
        backingFileId = event.getBackingFileId();
        ownerUserId = event.getOwningUserId();
    }

    @EventSourcingHandler
    void on(PhotoDeletedAsPartOfUserDeletionEvent event) {
        markDeleted();
    }

    private void validatePhotoBelongsToDeletedUser(DeletePhotoForDeletedUserCommand command,
                                                   AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        if (!command.getDeletedUserId().equals(ownerUserId)) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(DELETED_PHOTO_OWNER_MISMATCH, photoId, command.getDeletedUserId(), ownerUserId));
        }
    }
}
