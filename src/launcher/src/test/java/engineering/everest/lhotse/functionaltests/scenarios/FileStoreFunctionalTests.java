package engineering.everest.lhotse.functionaltests.scenarios;

import com.hazelcast.core.HazelcastInstance;
import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.tasks.PeriodicFilesMarkedForDeletionRemovalTask;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.persistence.FileMappingRepository;
import engineering.everest.starterkit.filestorage.persistence.PersistableFileMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
@Transactional
class FileStoreFunctionalTests {
    private static final byte[] TEMPORARY_FILE_CONTENTS = "A temporary file for unit testing".getBytes();

    @Autowired
    private FileService fileService;
    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private FileMappingRepository fileMappingRepository;

    private PeriodicFilesMarkedForDeletionRemovalTask periodicFilesMarkedForDeletionRemovalTask;

    @BeforeEach
    void setUp() throws IOException {
        fileService.transferToEphemeralStore(new ByteArrayInputStream(TEMPORARY_FILE_CONTENTS));

        periodicFilesMarkedForDeletionRemovalTask = new PeriodicFilesMarkedForDeletionRemovalTask(hazelcastInstance, fileService, 10);
    }

    @Test
    void ephemeralFilesMarkedForDeletionAreDeletedWhenDeleteTaskRuns() {
        fileService.markAllFilesForDeletion();

        periodicFilesMarkedForDeletionRemovalTask.deleteFilesInBatches();

        List<PersistableFileMapping> byMarkedForDeletionTrue = fileMappingRepository.findByMarkedForDeletionTrue(PageRequest.of(0, 10));
        assertTrue(byMarkedForDeletionTrue.isEmpty());
    }
}
