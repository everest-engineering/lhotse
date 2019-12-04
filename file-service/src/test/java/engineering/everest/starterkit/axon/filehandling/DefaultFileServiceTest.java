package engineering.everest.starterkit.axon.filehandling;

import engineering.everest.starterkit.axon.filehandling.persistence.FileMappingRepository;
import engineering.everest.starterkit.axon.filehandling.persistence.PersistableFileMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

import static engineering.everest.starterkit.axon.filehandling.FileStoreType.EPHEMERAL;
import static engineering.everest.starterkit.axon.filehandling.FileStoreType.PERMANENT;
import static engineering.everest.starterkit.axon.filehandling.NativeStorageType.MONGO_GRID_FS;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultFileServiceTest {

    private static final String ORIGINAL_FILENAME = "original-filename";

    private DefaultFileService fileService;

    @Mock
    private FileMappingRepository fileMappingRepository;
    @Mock
    private DeduplicatingFileStore permanentFileStore;
    @Mock
    private DeduplicatingFileStore ephemeralFileStore;

    @BeforeEach
    void setUp() {
        fileService = new DefaultFileService(fileMappingRepository, permanentFileStore, ephemeralFileStore);
    }

    @Test
    void createTempFile_WillCreateATemporaryFileMarkedAsDeleteOnExit() throws IOException {
        File temporaryFile = fileService.createTemporaryFile();

        assertTrue(temporaryFile.canWrite());
        assertTrue(temporaryFile.canRead());
    }

    @Test
    void transferToPermanentStorE_WillDelegateToPermanentStore() throws IOException {
        when(permanentFileStore.store(eq(ORIGINAL_FILENAME), any(InputStream.class))).thenReturn(new PersistedFile());

        File tempFile = fileService.createTemporaryFile();
        try (FileInputStream inputStream = new FileInputStream(tempFile)) {
            fileService.transferToPermanentStore(ORIGINAL_FILENAME, inputStream);
            verify(permanentFileStore).store(ORIGINAL_FILENAME, inputStream);
        }

        verifyNoInteractions(ephemeralFileStore);
    }

    @Test
    void transferToEphemeralStore_WillDelegateToEphemeralStore() throws IOException {
        when(ephemeralFileStore.store(eq(ORIGINAL_FILENAME), any(InputStream.class))).thenReturn(new PersistedFile());

        File tempFile = fileService.createTemporaryFile();
        try (FileInputStream inputStream = new FileInputStream(tempFile)) {
            fileService.transferToEphemeralStore(ORIGINAL_FILENAME, inputStream);
            verify(ephemeralFileStore).store(ORIGINAL_FILENAME, inputStream);
        }

        verifyNoInteractions(permanentFileStore);
    }

    @Test
    void transferToEphemeralStore_WillDelegateToEphemeralStore_WhenNoFilenamespecified() throws IOException {
        when(ephemeralFileStore.store(eq(""), any(InputStream.class))).thenReturn(new PersistedFile());

        File tempFile = fileService.createTemporaryFile();
        try (FileInputStream inputStream = new FileInputStream(tempFile)) {
            fileService.transferToEphemeralStore(inputStream);
            verify(ephemeralFileStore).store("", inputStream);
        }

        verifyNoInteractions(permanentFileStore);
    }

    @Test
    void stream_WillDelegateToPermanentFileStore_WhenFileMapsToPermanentStore() throws IOException {
        UUID fileId = randomUUID();
        PersistedFileIdentifier persistedFileIdentifier = new PersistedFileIdentifier(fileId, PERMANENT, MONGO_GRID_FS, "native-file-id");
        PersistableFileMapping persistableFileMapping = new PersistableFileMapping(fileId, PERMANENT, MONGO_GRID_FS, "native-file-id", "", "", 123L);
        ByteArrayInputStream inputStreamOngoingStubbing = new ByteArrayInputStream("hello".getBytes());

        when(fileMappingRepository.findById(persistedFileIdentifier.getFileId())).thenReturn(Optional.of(persistableFileMapping));
        when(permanentFileStore.stream(persistedFileIdentifier)).thenReturn(inputStreamOngoingStubbing);
        assertEquals(inputStreamOngoingStubbing, fileService.stream(persistedFileIdentifier.getFileId()));
    }

    @Test
    void stream_WillDelegateToEphemeralFileStore_WhenFileMapsToEphemeralStore() throws IOException {
        UUID fileId = randomUUID();
        PersistedFileIdentifier persistedFileIdentifier = new PersistedFileIdentifier(fileId, EPHEMERAL, MONGO_GRID_FS, "native-file-id");
        PersistableFileMapping persistableFileMapping = new PersistableFileMapping(fileId, EPHEMERAL, MONGO_GRID_FS, "native-file-id", "", "", 123L);
        ByteArrayInputStream inputStreamOngoingStubbing = new ByteArrayInputStream("hello".getBytes());

        when(fileMappingRepository.findById(persistedFileIdentifier.getFileId())).thenReturn(Optional.of(persistableFileMapping));
        when(ephemeralFileStore.stream(persistedFileIdentifier)).thenReturn(inputStreamOngoingStubbing);
        assertEquals(inputStreamOngoingStubbing, fileService.stream(persistedFileIdentifier.getFileId()));
    }
}
