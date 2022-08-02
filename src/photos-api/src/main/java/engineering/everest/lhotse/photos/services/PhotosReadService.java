package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.photos.Photo;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

public interface PhotosReadService {
    List<Photo> getAllPhotos(UUID requestingUserId, Pageable pageable);

    Photo getPhoto(UUID photoId);

    InputStream streamPhoto(UUID requestingUserId, UUID photoId) throws IOException;

    InputStream streamPhotoThumbnail(UUID requestingUserId, UUID photoId, int width, int height) throws IOException;
}
