package engineering.everest.lhotse.photos.handlers;

import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import engineering.everest.lhotse.photos.persistence.PhotosRepository;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.eventhandling.ResetHandler;
import org.axonframework.eventhandling.Timestamp;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Slf4j
public class PhotosEventHandler {

    private final PhotosRepository photosRepository;

    public PhotosEventHandler(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }

    @ResetHandler
    public void prepareForReplay() {
        LOGGER.info("{} deleting projections", PhotosRepository.class.getSimpleName());
        photosRepository.deleteAll();
    }

    @EventHandler
    void on(PhotoUploadedEvent event, @Timestamp Instant uploadTimestamp) {
        LOGGER.info("User {} uploaded photo {} (backing file {})", event.getOwningUserId(), event.getPhotoId(), event.getBackingFileId());
        photosRepository.createPhoto(event.getPhotoId(), event.getOwningUserId(), event.getBackingFileId(), event.getFilename(),
            uploadTimestamp);
    }
}
