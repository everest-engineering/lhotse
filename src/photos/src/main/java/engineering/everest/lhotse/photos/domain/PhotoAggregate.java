package engineering.everest.lhotse.photos.domain;

import engineering.everest.lhotse.photos.domain.commands.RegisterUploadedPhotoCommand;
import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.spring.stereotype.Aggregate;

import java.io.Serializable;
import java.util.UUID;

import static org.axonframework.modelling.command.AggregateLifecycle.apply;

@Aggregate(snapshotTriggerDefinition = "photoAggregateSnapshotTriggerDefinition")
public class PhotoAggregate implements Serializable {

    @AggregateIdentifier
    private UUID photoId;
    private UUID ownerUserId;

    PhotoAggregate() {}

    @CommandHandler
    PhotoAggregate(RegisterUploadedPhotoCommand command) {
        apply(new PhotoUploadedEvent(command.getPhotoId(), command.getOwningUserId(), command.getBackingFileId(), command.getFilename()));
    }

    @EventSourcingHandler
    void on(PhotoUploadedEvent event) {
        photoId = event.getPhotoId();
        ownerUserId = event.getOwningUserId();
    }
}
