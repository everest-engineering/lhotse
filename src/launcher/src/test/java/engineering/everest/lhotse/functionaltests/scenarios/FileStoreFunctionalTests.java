package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.tasks.PeriodicFilesMarkedForDeletionRemovalTask;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.persistence.FileMappingRepository;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Set;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
@Transactional
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("functionaltests")
class FileStoreFunctionalTests {
    private static final byte[] TEMPORARY_FILE_CONTENTS = "A temporary file for testing".getBytes();

    @Autowired
    @Qualifier("sharedEntityManager")
    private EntityManager entityManager;
    @Autowired
    private FileService fileService;
    @Autowired
    private ThumbnailService thumbnailService;
    @Autowired
    private FileMappingRepository fileMappingRepository;

    private PeriodicFilesMarkedForDeletionRemovalTask periodicFilesMarkedForDeletionRemovalTask;

    @BeforeEach
    void setUp() {
        fileMappingRepository.deleteAll();

        periodicFilesMarkedForDeletionRemovalTask = new PeriodicFilesMarkedForDeletionRemovalTask(entityManager,
            fileService, thumbnailService, 10);
    }

    @Test
    void ephemeralFilesMarkedForDeletionAreDeletedWhenDeleteTaskRuns() throws IOException {
        fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));
        fileService.markAllEphemeralFilesForDeletion();

        periodicFilesMarkedForDeletionRemovalTask.deleteFilesInBatches();

        var byMarkedForDeletionTrue = fileMappingRepository.findByMarkedForDeletionTrue(PageRequest.of(0, 10));
        assertTrue(byMarkedForDeletionTrue.isEmpty());
    }

    @Test
    void backingFileIsRetained_WhenDeduplicatedFileDeletedAndTaskRuns() throws Exception {
        var firstFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));
        var secondFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));

        fileService.markEphemeralFileForDeletion(firstFileId);

        periodicFilesMarkedForDeletionRemovalTask.deleteFilesInBatches();

        try (var inputStreamOfKnownLength = fileService.stream(secondFileId)) {
            assertArrayEquals(TEMPORARY_FILE_CONTENTS, inputStreamOfKnownLength.getInputStream().readAllBytes());
        }
    }

    @Test
    void filesWithSameContentAreDeduplicated() throws Exception {
        var firstFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));
        var secondFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));

        assertEquals(fileMappingRepository.findById(firstFileId).orElseThrow().getBackingStorageFileId(),
            fileMappingRepository.findById(secondFileId).orElseThrow().getBackingStorageFileId());
    }

    @Test
    void newBackingFileIsAssigned_WhenAllExistingDeduplicatedFilesMarkedAsDeleted() throws Exception {
        var firstFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));
        var secondFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));

        fileService.markEphemeralFilesForDeletion(Set.of(firstFileId, secondFileId));

        var thirdFileId = fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));

        assertNotEquals(fileMappingRepository.findById(firstFileId).orElseThrow().getBackingStorageFileId(),
            fileMappingRepository.findById(thirdFileId).orElseThrow().getBackingStorageFileId());

        periodicFilesMarkedForDeletionRemovalTask.deleteFilesInBatches();

        try (var inputStreamOfKnownLength = fileService.stream(thirdFileId)) {
            assertArrayEquals(TEMPORARY_FILE_CONTENTS, inputStreamOfKnownLength.getInputStream().readAllBytes());
        }
    }
}
