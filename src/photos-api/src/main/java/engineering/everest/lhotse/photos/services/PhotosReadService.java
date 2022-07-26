package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.photos.Photo;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface PhotosReadService {
    List<Photo> getAllPhotosForUser(UUID userId, Pageable pageable);
}
