package engineering.everest.lhotse.photos.handlers;

import engineering.everest.lhotse.photos.domain.events.PhotoDeletedAsPartOfUserDeletionEvent;
import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import engineering.everest.lhotse.photos.services.PhotosWriteService;
import engineering.everest.starterkit.filestorage.FileService;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class PhotosEventHandler {

    private final PhotosWriteService photosWriteService;
    private final FileService fileService;

    public PhotosEventHandler(PhotosWriteService photosWriteService, FileService fileService) {
        this.photosWriteService = photosWriteService;
        this.fileService = fileService;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("Deleting photos projections");
        photosWriteService.deleteAll();
    }

    @EventHandler
    void on(PhotoUploadedEvent event, @Timestamp Instant uploadTimestamp) {
        LOGGER.info("User {} uploaded photo {} (backing file {})", event.getOwningUserId(), event.getPhotoId(), event.getPersistedFileId());
        photosWriteService.createPhoto(event.getPhotoId(), event.getOwningUserId(), event.getPersistedFileId(), event.getFilename(),
            uploadTimestamp);
    }

    @EventHandler
    void on(PhotoDeletedAsPartOfUserDeletionEvent event) {
        LOGGER.info("Deleting photo {} (backing file {}) for deleted user {}", event.getPhotoId(), event.getPersistedFileId(),
            event.getDeletedUserId());
        fileService.markEphemeralFileForDeletion(event.getPersistedFileId());
        photosWriteService.deleteById(event.getPhotoId());
    }
}
