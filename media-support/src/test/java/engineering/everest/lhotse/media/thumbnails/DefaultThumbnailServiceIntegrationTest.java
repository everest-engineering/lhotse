package engineering.everest.lhotse.media.thumbnails;

import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.lhotse.media.thumbnails.persistence.PersistableThumbnail;
import engineering.everest.lhotse.media.thumbnails.persistence.PersistableThumbnailMapping;
import engineering.everest.lhotse.media.thumbnails.persistence.ThumbnailMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.AutoConfigureDataMongo;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static java.lang.Thread.currentThread;
import static java.nio.file.Files.createTempFile;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@AutoConfigureDataMongo
@ExtendWith(SpringExtension.class)
@ComponentScan(basePackages = "engineering.everest.lhotse.media.thumbnails")
class DefaultThumbnailServiceIntegrationTest {

    private static final int SMALL_WIDTH = 600;
    private static final int SMALL_HEIGHT = 400;
    private static final int LARGE_WIDTH = 800;
    private static final int LARGE_HEIGHT = 600;
    private static final int HUGE_DIMENSION = 90000;
    private static final UUID SOURCE_FILE_ID_1 = randomUUID();
    private static final UUID SOURCE_FILE_ID_2 = randomUUID();
    private static final UUID SOURCE_FILE_1_THUMBNAIL_ID_1 = randomUUID();
    private static final UUID SOURCE_FILE_1_THUMBNAIL_ID_2 = randomUUID();
    private static final UUID SOURCE_FILE_2_THUMBNAIL_ID_1 = randomUUID();
    private static final PersistableThumbnail FILE_1_THUMBNAIL_1 = new PersistableThumbnail(SOURCE_FILE_1_THUMBNAIL_ID_1, SMALL_WIDTH, SMALL_HEIGHT);
    private static final PersistableThumbnail FILE_1_THUMBNAIL_2 = new PersistableThumbnail(SOURCE_FILE_1_THUMBNAIL_ID_2, LARGE_WIDTH, LARGE_HEIGHT);
    private static final PersistableThumbnail FILE_2_THUMBNAIL_1 = new PersistableThumbnail(SOURCE_FILE_2_THUMBNAIL_ID_1, SMALL_WIDTH, SMALL_HEIGHT);

    private ThumbnailService thumbnailService;

    @Autowired
    private ThumbnailMappingRepository thumbnailMappingRepository;
    @MockBean
    private FileService fileService;

    private InputStream file1Thumbnail1InputStream;
    private InputStream file1Thumbnail2InputStream;
    private InputStream file2Thumbnail1InputStream;

    @BeforeEach
    void setUp() throws IOException {
        thumbnailService = new DefaultThumbnailService(fileService, thumbnailMappingRepository);

        thumbnailMappingRepository.save(new PersistableThumbnailMapping(SOURCE_FILE_ID_1, List.of(FILE_1_THUMBNAIL_1, FILE_1_THUMBNAIL_2)));
        thumbnailMappingRepository.save(new PersistableThumbnailMapping(SOURCE_FILE_ID_2, List.of(FILE_2_THUMBNAIL_1)));

        file1Thumbnail1InputStream = new ByteArrayInputStream("file-1-thumbnail-1-file-contents".getBytes());
        file1Thumbnail2InputStream = new ByteArrayInputStream("file-1-thumbnail-2-file-contents".getBytes());
        file2Thumbnail1InputStream = new ByteArrayInputStream("file-2-thumbnail-1-file-contents".getBytes());

        when(fileService.createTemporaryFile()).thenReturn(createTempFile("unit", "test").toFile());
        when(fileService.stream(SOURCE_FILE_1_THUMBNAIL_ID_1)).thenReturn(file1Thumbnail1InputStream);
        when(fileService.stream(SOURCE_FILE_1_THUMBNAIL_ID_2)).thenReturn(file1Thumbnail2InputStream);
        when(fileService.stream(SOURCE_FILE_2_THUMBNAIL_ID_1)).thenReturn(file2Thumbnail1InputStream);
    }

    @Test
    void willStreamExistingThumbnailForGivenDimension_WhenCachedInEphemeralFileStore() throws IOException {
        assertEquals(file1Thumbnail1InputStream, thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, SMALL_WIDTH, SMALL_HEIGHT));
        assertEquals(file1Thumbnail2InputStream, thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, LARGE_WIDTH, LARGE_HEIGHT));
        assertEquals(file2Thumbnail1InputStream, thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_2, SMALL_WIDTH, SMALL_HEIGHT));

        verify(fileService, never()).transferToEphemeralStore(anyString(), any(InputStream.class));
        verify(fileService, never()).transferToEphemeralStore(any(InputStream.class));
    }

    @Test
    void willCreateThumbnailOnTheFlyAndPersistInEphemeralStore_WhenSourceFileHasNoCachedThumbnails() throws IOException {
        UUID newSourceFileId = randomUUID();
        UUID newThumbnailFileId = randomUUID();

        String expectedThumbnailFilename = String.format("%s-thumbnail-%sx%s.png", newSourceFileId, SMALL_WIDTH, SMALL_HEIGHT);
        when(fileService.stream(newSourceFileId)).thenReturn(getTestInputStream("new-image.jpg"));
        when(fileService.transferToEphemeralStore(eq(expectedThumbnailFilename), any(InputStream.class))).thenReturn(newThumbnailFileId);
        when(fileService.stream(newSourceFileId)).thenReturn(getTestInputStream("new-image.jpg"));
        InputStream newThumbnailInputStream = mock(InputStream.class);
        when(fileService.stream(newThumbnailFileId)).thenReturn(newThumbnailInputStream);

        assertEquals(newThumbnailInputStream, thumbnailService.streamThumbnailForOriginalFile(newSourceFileId, SMALL_WIDTH, SMALL_HEIGHT));

        var expectedThumbnailMapping = new PersistableThumbnailMapping(newSourceFileId, Collections.singletonList(new PersistableThumbnail(newThumbnailFileId, SMALL_WIDTH, SMALL_HEIGHT)));
        assertEquals(expectedThumbnailMapping, thumbnailMappingRepository.findById(newSourceFileId).orElseThrow());
    }

    @Test
    void willUpdateThumbnailsForExistingSourceFile_WhenThumbnailsExistForDifferentDimensions() throws IOException {
        UUID newThumbnailFileId = randomUUID();

        int newWidth = 1234;
        int newHeight = 5678;
        String expectedThumbnailFilename = String.format("%s-thumbnail-%sx%s.png", SOURCE_FILE_ID_1, newWidth, newHeight);
        when(fileService.transferToEphemeralStore(eq(expectedThumbnailFilename), any(InputStream.class))).thenReturn(newThumbnailFileId);
        when(fileService.stream(SOURCE_FILE_ID_1)).thenReturn(getTestInputStream("existing-image.jpg"));
        InputStream newThumbnailInputStream = mock(InputStream.class);
        when(fileService.stream(newThumbnailFileId)).thenReturn(newThumbnailInputStream);

        assertEquals(newThumbnailInputStream, thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, newWidth, newHeight));

        var expectedThumbnailMapping = new PersistableThumbnailMapping(SOURCE_FILE_ID_1,
                List.of(FILE_1_THUMBNAIL_1, FILE_1_THUMBNAIL_2, new PersistableThumbnail(newThumbnailFileId, newWidth, newHeight)));
        assertEquals(expectedThumbnailMapping, thumbnailMappingRepository.findById(SOURCE_FILE_ID_1).orElseThrow());
    }

    @Test
    void willFail_WhenThumbnailSizeIsTooSmall() {
        assertThrows(IllegalArgumentException.class, () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, SMALL_WIDTH, 0));
        assertThrows(IllegalArgumentException.class, () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, 0, SMALL_HEIGHT));
    }

    @Test
    @Disabled("implementation is pending push elsewhere")
    void willFail_WhenThumbnailSizeIsTooLarge() {
        assertThrows(IllegalArgumentException.class, () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, SMALL_WIDTH, HUGE_DIMENSION));
        assertThrows(IllegalArgumentException.class, () -> thumbnailService.streamThumbnailForOriginalFile(SOURCE_FILE_ID_1, HUGE_DIMENSION, SMALL_HEIGHT));
    }

    private InputStream getTestInputStream(String filename) {
        return currentThread().getContextClassLoader().getResourceAsStream(filename);
    }
}