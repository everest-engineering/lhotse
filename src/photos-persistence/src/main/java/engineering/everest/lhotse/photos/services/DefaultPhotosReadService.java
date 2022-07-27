package engineering.everest.lhotse.photos.services;

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

import static java.util.stream.Collectors.toList;

@Service
public class DefaultPhotosReadService implements PhotosReadService {

    private final PhotosRepository photosRepository;
    private final FileService fileService;
    private final ThumbnailService thumbnailService;

    public DefaultPhotosReadService(PhotosRepository photosRepository, FileService fileService, ThumbnailService thumbnailService) {
        this.photosRepository = photosRepository;
        this.fileService = fileService;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public List<Photo> getAllPhotos(UUID requestingUserId, Pageable pageable) {
        return photosRepository.findByOwnerUserId(requestingUserId, pageable).stream()
            .map(PersistablePhoto::toDomain)
            .collect(toList());
    }

    @Override
    public InputStream streamPhoto(UUID requestingUserId, UUID photoId) throws IOException {
        var persistablePhoto = photosRepository.findByIdAndOwnerUserId(photoId, requestingUserId).orElseThrow();
        return fileService.stream(persistablePhoto.getBackingFileId()).getInputStream();
    }

    @Override
    public InputStream streamPhotoThumbnail(UUID requestingUserId, UUID photoId, int width, int height) throws IOException {
        var persistablePhoto = photosRepository.findByIdAndOwnerUserId(photoId, requestingUserId).orElseThrow();
        return thumbnailService.streamThumbnailForOriginalFile(persistablePhoto.getBackingFileId(), width, height);
    }
}
