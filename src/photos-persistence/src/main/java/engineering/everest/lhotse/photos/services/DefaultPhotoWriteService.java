package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.photos.persistence.PhotosRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
public class DefaultPhotoWriteService implements PhotosWriteService {

    private final PhotosRepository photosRepository;

    DefaultPhotoWriteService(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }

    @Override
    public void createPhoto(UUID id, UUID ownerUserId, UUID persistedFileId, String filename, Instant uploadTimestamp) {
        photosRepository.createPhoto(id, ownerUserId, persistedFileId, filename, uploadTimestamp);
    }

    @Override
    public void deleteAll() {
        photosRepository.deleteAll();
    }

    @Override
    public void deleteById(UUID id) {
        photosRepository.deleteById(id);
    }
}
