package engineering.everest.lhotse.axon.filehandling;

import engineering.everest.lhotse.axon.filehandling.persistence.FileMappingRepository;
import engineering.everest.lhotse.axon.filehandling.persistence.PersistableFileMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static engineering.everest.lhotse.axon.filehandling.FileStoreType.PERMANENT;
import static engineering.everest.lhotse.axon.filehandling.NativeStorageType.MONGO_GRID_FS;
import static java.nio.file.Files.createTempFile;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NativeDeduplicatingFileStoreTest {

    private static final String ORIGINAL_FILENAME = "original-filename";
    private static final String EXISTING_NATIVE_STORE_FILE_ID = "existing-native-store-file-id";
    private static final String SHA_256 = "108e0047119fdf8db72dc146283d0cd717d620a9b4fb9ead902e22f4c04fbe7b";
    private static final String SHA_512 = "cb61c18674f50eedd4f7d77f938b11d468713516b14862c4ae4ea68ec5aa30c1475d7d38f17e14585da10ea848a054733f2185b1ea57f10a1c416bb1617baa60";
    private static final String TEMPORARY_FILE_CONTENTS = "A temporary file for unit testing";
    private static final Long FILE_SIZE = (long) TEMPORARY_FILE_CONTENTS.length();

    private NativeDeduplicatingFileStore nativeDeduplicatingFileStore;
    private String fileIdentifier;

    @Mock
    private FileStore fileStore;
    @Mock
    private FileMappingRepository fileMappingRepository;

    @BeforeEach
    void setUp() {
        nativeDeduplicatingFileStore = new NativeDeduplicatingFileStore(PERMANENT, fileMappingRepository, fileStore);
        fileIdentifier = "FILE_ID";

        when(fileStore.nativeStorageType()).thenReturn(MONGO_GRID_FS);
        when(fileStore.create(any(InputStream.class), eq(ORIGINAL_FILENAME))).thenAnswer(invocation -> {
            InputStream inputFile = invocation.getArgument(0);
            inputFile.readAllBytes();
            inputFile.close();
            return fileIdentifier;
        });
    }

    @Test
    void store_WillPersistAndReturnNativeStorageEncodingFileId() throws IOException {
        PersistedFile persistedFile = nativeDeduplicatingFileStore.store(ORIGINAL_FILENAME, createTempFileWithContents());

        verifyNoMoreInteractions(fileStore);
        verify(fileMappingRepository).save(new PersistableFileMapping(persistedFile.getFileId(), PERMANENT, MONGO_GRID_FS, fileIdentifier, SHA_256, SHA_512, FILE_SIZE));
        PersistedFile expectedPersistedFile = new PersistedFile(persistedFile.getFileId(), PERMANENT, MONGO_GRID_FS, fileIdentifier, SHA_256, SHA_512, FILE_SIZE);
        assertEquals(expectedPersistedFile, persistedFile);
    }

    @Test
    void store_WillDeduplicate_WhenFileAlreadyPresentInStore() throws IOException {
        when(fileMappingRepository.findOne(any(Example.class))).thenReturn(
                Optional.of(new PersistableFileMapping(randomUUID(), PERMANENT, MONGO_GRID_FS, EXISTING_NATIVE_STORE_FILE_ID, SHA_256, SHA_512, FILE_SIZE)));

        PersistedFile persistedFile = nativeDeduplicatingFileStore.store(ORIGINAL_FILENAME, createTempFileWithContents());

        verify(fileStore).delete(fileIdentifier);
        verify(fileMappingRepository).save(new PersistableFileMapping(persistedFile.getFileId(), PERMANENT, MONGO_GRID_FS, EXISTING_NATIVE_STORE_FILE_ID, SHA_256, SHA_512, FILE_SIZE));
        PersistedFile expectedPersistedFile = new PersistedFile(persistedFile.getFileId(), PERMANENT, MONGO_GRID_FS, EXISTING_NATIVE_STORE_FILE_ID, SHA_256, SHA_512, FILE_SIZE);
        assertEquals(expectedPersistedFile, persistedFile);
    }

    private InputStream createTempFileWithContents() throws IOException {
        Path tempPath = createTempFile("unit", "test");
        try (var outStream = Files.newOutputStream(tempPath)) {
            outStream.write(TEMPORARY_FILE_CONTENTS.getBytes());
            outStream.flush();
        }
        return new FileInputStream(tempPath.toFile());
    }
}
