package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.persistence.PersistablePhoto;
import engineering.everest.lhotse.photos.persistence.PhotosRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@Service
public class DefaultPhotosReadService implements PhotosReadService {

    private final PhotosRepository photosRepository;

    public DefaultPhotosReadService(PhotosRepository photosRepository) {
        this.photosRepository = photosRepository;
    }

    @Override
    public List<Photo> getAllPhotosForUser(UUID userId, Pageable pageable) {
        return photosRepository.findByOwnerUserId(userId, pageable).stream()
            .map(PersistablePhoto::toDomain)
            .collect(toList());
    }
}
