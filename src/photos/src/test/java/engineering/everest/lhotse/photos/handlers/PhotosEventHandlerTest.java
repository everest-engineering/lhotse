package engineering.everest.lhotse.photos.handlers;

import engineering.everest.lhotse.photos.domain.events.PhotoDeletedAsPartOfUserDeletionEvent;
import engineering.everest.lhotse.photos.domain.events.PhotoUploadedEvent;
import engineering.everest.lhotse.photos.persistence.PhotosRepository;
import engineering.everest.starterkit.filestorage.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PhotosEventHandlerTest {

    private static final UUID PHOTO_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID BACKING_FILE_ID = randomUUID();
    private static final String PHOTO_FILENAME = "holiday snap.png";
    private static final Instant UPLOAD_TIMESTAMP = Instant.ofEpochSecond(123);

    private PhotosEventHandler photosEventHandler;

    @Mock
    private PhotosRepository photosRepository;
    @Mock
    private FileService fileService;

    @BeforeEach
    void setUp() {
        photosEventHandler = new PhotosEventHandler(photosRepository, fileService);
    }

    @Test
    void prepareForReplay_WillClearProjection() {
        photosEventHandler.prepareForReplay();
        verify(photosRepository).deleteAll();
    }

    @Test
    void onPhotoUploadedEvent_WillProject() {
        photosEventHandler.on(new PhotoUploadedEvent(PHOTO_ID, USER_ID, BACKING_FILE_ID, PHOTO_FILENAME), UPLOAD_TIMESTAMP);
        verify(photosRepository).createPhoto(PHOTO_ID, USER_ID, BACKING_FILE_ID, PHOTO_FILENAME, UPLOAD_TIMESTAMP);
    }

    @Test
    void onPhotoDeletedAsPartOfUserDeletionEvent_WillDeletePhoto() {
        photosEventHandler.on(new PhotoDeletedAsPartOfUserDeletionEvent(PHOTO_ID, BACKING_FILE_ID, USER_ID));
        verify(photosRepository).deleteById(PHOTO_ID);
    }

    @Test
    void onPhotoDeletedAsPartOfUserDeletionEvent_WillMarkBackingFileForDeletion() {
        photosEventHandler.on(new PhotoDeletedAsPartOfUserDeletionEvent(PHOTO_ID, BACKING_FILE_ID, USER_ID));
        verify(fileService).markEphemeralFileForDeletion(BACKING_FILE_ID);
    }
}
