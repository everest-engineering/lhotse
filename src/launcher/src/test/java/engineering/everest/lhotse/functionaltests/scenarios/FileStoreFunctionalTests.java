package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.tasks.PeriodicFilesMarkedForDeletionRemovalTask;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.persistence.FileMappingRepository;
import engineering.everest.starterkit.filestorage.persistence.PersistableFileMapping;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
@Transactional
@DirtiesContext
@ExtendWith(MockitoExtension.class)
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
    void setUp() throws IOException {
        fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));

        periodicFilesMarkedForDeletionRemovalTask =
            new PeriodicFilesMarkedForDeletionRemovalTask(entityManager, fileService, thumbnailService, 10);
    }

    @Test
    void ephemeralFilesMarkedForDeletionAreDeletedWhenDeleteTaskRuns() {
        fileService.markAllEphemeralFilesForDeletion();

        periodicFilesMarkedForDeletionRemovalTask.deleteFilesInBatches();

        List<PersistableFileMapping> byMarkedForDeletionTrue = fileMappingRepository.findByMarkedForDeletionTrue(PageRequest.of(0, 10));
        assertTrue(byMarkedForDeletionTrue.isEmpty());
    }
}
