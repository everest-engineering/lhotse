package engineering.everest.lhotse.photos.persistence;

import engineering.everest.lhotse.photos.Photo;
import engineering.everest.lhotse.photos.persistence.config.TestPhotosJpaConfig;
import engineering.everest.lhotse.photos.services.DefaultPhotoWriteService;
import engineering.everest.lhotse.photos.services.DefaultPhotosReadService;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@AutoConfigureEmbeddedDatabase(refresh = AFTER_EACH_TEST_METHOD, type = POSTGRES)
@DataJpaTest
@EnableAutoConfiguration
@ComponentScan(basePackages = "engineering.everest.lhotse.photos")
@ContextConfiguration(classes = { TestPhotosJpaConfig.class })
public class DefaultPhotosWriteServiceIntegrationTest {

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
    private static final Photo PHOTO_3 = new Photo(PHOTO_ID_3, USER_ID_1, BACKING_FILE_ID_3, "photo3.png", Instant.ofEpochMilli(456));
    private static final String PHOTO_FILE_CONTENTS = "my holiday snap bits";
    private static final String PHOTO_1_NAME = "photo1.png";
    private static final String PHOTO_2_NAME = "photo2.png";
    private static final String PHOTO_3_NAME = "photo3.png";

    @Autowired
    private DefaultPhotoWriteService photosWriteService;
    @Autowired
    private DefaultPhotosReadService photosReadService;

    @MockBean
    private FileService fileService;
    @MockBean
    private ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() {
        photosWriteService.createPhoto(PHOTO_ID_1, USER_ID_1, BACKING_FILE_ID_1, PHOTO_1_NAME, Instant.ofEpochMilli(123));
        photosWriteService.createPhoto(PHOTO_ID_2, USER_ID_1, BACKING_FILE_ID_2, PHOTO_2_NAME, Instant.ofEpochMilli(456));
    }

    @Test
    void createPhoto_WillPersistPhotos() {
        photosWriteService.createPhoto(PHOTO_ID_3, USER_ID_1, BACKING_FILE_ID_3, PHOTO_3_NAME, Instant.ofEpochMilli(456));

        assertEquals(List.of(PHOTO_1, PHOTO_2, PHOTO_3), photosReadService.getAllPhotos(USER_ID_1, Pageable.unpaged()));
    }

    @Test
    void deleteAll_WillDeleteAllPersistedPhotos() {
        photosWriteService.createPhoto(PHOTO_ID_3, USER_ID_2, BACKING_FILE_ID_3, PHOTO_3_NAME, Instant.ofEpochMilli(456));

        photosWriteService.deleteAll();

        assertTrue(photosReadService.getAllPhotos(USER_ID_1, Pageable.unpaged()).isEmpty());
        assertTrue(photosReadService.getAllPhotos(USER_ID_2, Pageable.unpaged()).isEmpty());
    }

    @Test
    void deleteById_WillDeletePersistedPhotoById() {
        photosWriteService.deleteById(PHOTO_ID_1);

        assertEquals(List.of(PHOTO_2), photosReadService.getAllPhotos(USER_ID_1, Pageable.unpaged()));
    }
}
