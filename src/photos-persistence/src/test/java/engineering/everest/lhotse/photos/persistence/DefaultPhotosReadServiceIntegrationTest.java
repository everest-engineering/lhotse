package engineering.everest.lhotse.photos.persistence;

import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.persistence.config.TestPhotosJpaConfig;
import engineering.everest.lhotse.photos.services.DefaultPhotosReadService;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.InputStreamOfKnownLength;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.parallel.ExecutionMode.SAME_THREAD;
import static org.mockito.Mockito.when;
import static org.springframework.data.domain.Sort.Direction.DESC;

@AutoConfigureEmbeddedDatabase(refresh = AFTER_EACH_TEST_METHOD, type = POSTGRES)
@DataJpaTest
@EnableAutoConfiguration
@ComponentScan(basePackages = "engineering.everest.lhotse.photos")
@ContextConfiguration(classes = { TestPhotosJpaConfig.class })
@Execution(SAME_THREAD)
public class DefaultPhotosReadServiceIntegrationTest {

    private static final UUID PHOTO_ID_1 = UUID.randomUUID();
    private static final UUID PHOTO_ID_2 = UUID.randomUUID();
    private static final UUID PHOTO_ID_3 = UUID.randomUUID();
    private static final UUID USER_ID_1 = UUID.randomUUID();
    private static final UUID USER_ID_2 = UUID.randomUUID();
    private static final UUID BACKING_FILE_ID_1 = UUID.randomUUID();
    private static final UUID BACKING_FILE_ID_2 = UUID.randomUUID();
    private static final UUID BACKING_FILE_ID_3 = UUID.randomUUID();
    private static final Photo PHOTO_1 = new Photo(PHOTO_ID_1, USER_ID_1, BACKING_FILE_ID_1, "photo1.png", Instant.ofEpochMilli(123));
    private static final Photo PHOTO_2 = new Photo(PHOTO_ID_2, USER_ID_1, BACKING_FILE_ID_2, "photo2.png", Instant.ofEpochMilli(456));
    private static final String PHOTO_FILE_CONTENTS = "my holiday snap bits";

    @Autowired
    private PhotosRepository photosRepository;
    @Autowired
    private DefaultPhotosReadService photosReadService;

    @MockBean
    private FileService fileService;
    @MockBean
    private ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() {
        photosRepository.createPhoto(PHOTO_ID_1, USER_ID_1, BACKING_FILE_ID_1, "photo1.png", Instant.ofEpochMilli(123));
        photosRepository.createPhoto(PHOTO_ID_2, USER_ID_1, BACKING_FILE_ID_2, "photo2.png", Instant.ofEpochMilli(456));
        photosRepository.createPhoto(PHOTO_ID_3, USER_ID_2, BACKING_FILE_ID_3, "photo3.png", Instant.ofEpochMilli(789));
    }

    @Test
    void getAllPhotosForUser_WillReturnPhotosOwnedByUser() {
        var expectedPhotos = List.of(PHOTO_1, PHOTO_2);
        assertEquals(expectedPhotos, photosReadService.getAllPhotos(USER_ID_1, Pageable.unpaged()));
    }

    @Test
    void getAllPhotosForUser_WillRetrievePagesWhenRequestedRespectingOrder() {
        var firstPage = photosReadService.getAllPhotos(USER_ID_1, PageRequest.of(0, 1, DESC, "uploadTimestamp"));
        var secondPage = photosReadService.getAllPhotos(USER_ID_1, PageRequest.of(1, 1, DESC, "uploadTimestamp"));

        assertEquals(List.of(PHOTO_2), firstPage);
        assertEquals(List.of(PHOTO_1), secondPage);
    }

    @Test
    void streamPhoto_WillReturnStream() throws IOException {
        var inputStream = new ByteArrayInputStream(PHOTO_FILE_CONTENTS.getBytes());
        when(fileService.stream(BACKING_FILE_ID_1)).thenReturn(
            new InputStreamOfKnownLength(inputStream, PHOTO_FILE_CONTENTS.length()));

        assertEquals(inputStream, photosReadService.streamPhoto(USER_ID_1, PHOTO_ID_1));
    }

    @Test
    void streamPhoto_WillFail_WhenPhotoNotAccessibleToRequestingUser() {
        assertThrows(NoSuchElementException.class, () -> photosReadService.streamPhoto(USER_ID_1, PHOTO_ID_3));
    }

    @Test
    void getProfilePhotoThumbnailStream_WillReturnStreamForProfilePhoto() throws IOException {
        var inputStream = new ByteArrayInputStream("thumbnail contents".getBytes());
        when(thumbnailService.streamThumbnailForOriginalFile(BACKING_FILE_ID_1, 100, 100)).thenReturn(inputStream);

        assertEquals(inputStream, photosReadService.streamPhotoThumbnail(USER_ID_1, PHOTO_ID_1, 100, 100));
    }

    @Test
    void streamPhotoThumbnail_WillFail_WhenPhotoNotAccessibleToRequestingUser() {
        assertThrows(NoSuchElementException.class, () -> photosReadService.streamPhotoThumbnail(USER_ID_1, PHOTO_ID_3, 100, 100));
    }
}
