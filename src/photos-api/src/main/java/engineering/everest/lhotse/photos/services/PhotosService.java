package engineering.everest.lhotse.photos.services;

import java.util.UUID;

public interface PhotosService {

    UUID registerUploadedPhoto(UUID persistedFileId, String filename);
}
