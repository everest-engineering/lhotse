package engineering.everest.lhotse.photos.services;

import engineering.everest.lhotse.common.AuthenticatedUser;
import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.persistence.PersistablePhoto;
import engineering.everest.lhotse.photos.persistence.PhotosRepository;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class DefaultPhotosReadService implements PhotosReadService {

    private final PhotosRepository photosRepository;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;

    private final AuthenticatedUser authenticatedUser;

    public DefaultPhotosReadService(PhotosRepository photosRepository,
                                    FileService fileService,
                                    ThumbnailService thumbnailService,
                                    AuthenticatedUser authenticatedUser) {
        this.photosRepository = photosRepository;
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
        this.authenticatedUser = authenticatedUser;
    }

    @Override
    public List<Photo> getAllPhotos(Pageable pageable) {
        var requestingUserId = authenticatedUser.getUserId();
        return photosRepository.findByOwnerUserId(requestingUserId, pageable).stream()
            .map(PersistablePhoto::toDomain)
            .toList();
    }

    @Override
    public Photo getPhoto(UUID photoId) {
        return photosRepository.findById(photoId).orElseThrow().toDomain();
    }

    @Override
    public InputStream streamPhoto(UUID photoId) throws IOException {
        var requestingUserId = authenticatedUser.getUserId();
        var persistablePhoto = photosRepository.findByIdAndOwnerUserId(photoId, requestingUserId).orElseThrow();
        return fileService.stream(persistablePhoto.getPersistedFileId()).getInputStream();
    }

    @Override
    public InputStream streamPhotoThumbnail(UUID photoId, int width, int height) throws IOException {
        var requestingUserId = authenticatedUser.getUserId();
        var persistablePhoto = photosRepository.findByIdAndOwnerUserId(photoId, requestingUserId).orElseThrow();
        return thumbnailService.streamThumbnailForOriginalFile(persistablePhoto.getPersistedFileId(), width, height);
    }
}
