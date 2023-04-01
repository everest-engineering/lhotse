package engineering.everest.lhotse.photos.domain;

import engineering.everest.lhotse.i18n.exceptions.TranslatableException;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.photos.domain.commands.DeletePhotoForDeletedUserCommand;
import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import engineering.everest.lhotse.photos.domain.events.PhotoDeletedAsPartOfUserDeletionEvent;
import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import org.axonframework.commandhandling.CommandExecutionException;
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
    private UUID persistedFileId;
    private UUID ownerUserId;

    PhotoAggregate() {}

    @CommandHandler
    PhotoAggregate(RegisterUploadedPhotoCommand command) {
        apply(new PhotoUploadedEvent(command.getPhotoId(), command.getOwningUserId(), command.getPersistedFileId(), command.getFilename()));
    }

    @CommandHandler
    void handle(DeletePhotoForDeletedUserCommand command) {
        validatePhotoBelongsToDeletedUser(command);

        apply(new PhotoDeletedAsPartOfUserDeletionEvent(photoId, persistedFileId, command.getDeletedUserId()));
    }

    @EventSourcingHandler
    void on(PhotoUploadedEvent event) {
        photoId = event.getPhotoId();
        persistedFileId = event.getPersistedFileId();
        ownerUserId = event.getOwningUserId();
    }

    @EventSourcingHandler
    void on(PhotoDeletedAsPartOfUserDeletionEvent event) {
        markDeleted();
    }

    private void throwWrappedInCommandExecutionException(TranslatableException translatableException) {
        throw new CommandExecutionException(translatableException.getMessage(), null, translatableException);
    }

    private void validatePhotoBelongsToDeletedUser(DeletePhotoForDeletedUserCommand command) {
        if (!command.getDeletedUserId().equals(ownerUserId)) {
            throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(DELETED_PHOTO_OWNER_MISMATCH, photoId, command.getDeletedUserId(), ownerUserId));
        }
    }
}
