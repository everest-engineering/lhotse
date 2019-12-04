package engineering.everest.starterkit.media.thumbnails;

import engineering.everest.starterkit.axon.filehandling.FileService;
import engineering.everest.starterkit.media.thumbnails.persistence.PersistableThumbnail;
import engineering.everest.starterkit.media.thumbnails.persistence.PersistableThumbnailMapping;
import engineering.everest.starterkit.media.thumbnails.persistence.ThumbnailMappingRepository;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;
import static net.coobird.thumbnailator.Thumbnailator.createThumbnail;

@Component
public class DefaultThumbnailService implements ThumbnailService {

    private final FileService fileService;
    private final ThumbnailMappingRepository thumbnailMappingRepository;

    public DefaultThumbnailService(FileService fileService, ThumbnailMappingRepository thumbnailMappingRepository) {
        this.fileService = fileService;
        this.thumbnailMappingRepository = thumbnailMappingRepository;
    }

    @Override
    public InputStream streamThumbnailForOriginalFile(UUID fileId, int width, int height) throws IOException {
        var existingMapping = findExistingThumbnail(fileId, width, height);

        var thumbnailFileId = existingMapping.isPresent()
                ? existingMapping.get().getThumbnailFileId()
                : createThumbnailForOriginalFile(fileId, width, height);

        return fileService.stream(thumbnailFileId);
    }

    private Optional<PersistableThumbnail> findExistingThumbnail(UUID fileId, int width, int height) {
        Optional<PersistableThumbnailMapping> thumbnailMapping = thumbnailMappingRepository.findById(fileId);

        if (thumbnailMapping.isPresent()) {
            return thumbnailMapping.get().getThumbnails().stream()
                    .filter(x -> x.getWidth() == width && x.getHeight() == height)
                    .findFirst();
        }
        return Optional.empty();
    }

    private UUID createThumbnailForOriginalFile(UUID fileId, int width, int height) throws IOException {
        if (width < 1 || height < 1) {
            throw new IllegalArgumentException("Thumbnail dimension can't be less than 1");
        }

        var tempFile = fileService.createTemporaryFile();
        try (var originalInputStream = fileService.stream(fileId);
             var thumbnailOutputStream = newOutputStream(tempFile.toPath())) {
            createThumbnail(originalInputStream, thumbnailOutputStream, width, height);
            thumbnailOutputStream.close();
            return persistThumbnailAndUpdateMapping(fileId, width, height, tempFile,
                    String.format("%s-thumbnail-%sx%s.png", fileId, width, height));
        } finally {
            tempFile.delete();
        }
    }

    private UUID persistThumbnailAndUpdateMapping(UUID originalFileId, int width, int height,
                                                  File tempFile, String thumbnailFilename) throws IOException {
        try (var thumbnailInputStream = newInputStream(tempFile.toPath())) {
            var thumbnailFileID = fileService.transferToEphemeralStore(thumbnailFilename, thumbnailInputStream);
            var thumbnailMapping = thumbnailMappingRepository.findById(originalFileId)
                    .orElseGet(() -> new PersistableThumbnailMapping(originalFileId, new ArrayList<>()));
            thumbnailMapping.addThumbnail(thumbnailFileID, width, height);
            thumbnailMappingRepository.save(thumbnailMapping);
            return thumbnailFileID;
        }
    }
}
