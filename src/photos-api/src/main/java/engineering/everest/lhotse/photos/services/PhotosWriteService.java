package engineering.everest.lhotse.photos.services;

import java.time.Instant;
import java.util.UUID;

public interface PhotosWriteService {
    void createPhoto(UUID id, UUID ownerUserId, UUID persistedFileId, String filename, Instant uploadTimestamp);

    void deleteAll();

    void deleteById(UUID id);
}
