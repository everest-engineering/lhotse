package engineering.everest.lhotse.functionaltests.scenarios;

import com.hazelcast.core.HazelcastInstance;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.process.runtime.Network;
import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.tasks.PeriodicFilesMarkedForDeletionRemovalTask;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.persistence.FileMappingRepository;
import engineering.everest.starterkit.filestorage.persistence.PersistableFileMapping;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import javax.transaction.Transactional;
import java.util.List;
import java.util.UUID;

import static de.flapdoodle.embed.mongo.distribution.Version.Main.PRODUCTION;
import static engineering.everest.starterkit.filestorage.FileStoreType.EPHEMERAL;
import static engineering.everest.starterkit.filestorage.NativeStorageType.MONGO_GRID_FS;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
@Transactional
class FileStoreFunctionalTests {

    private static final String SHA_256 = "108e0047119fdf8db72dc146283d0cd717d620a9b4fb9ead902e22f4c04fbe7b";
    private static final String SHA_512 = "cb61c18674f50eedd4f7d77f938b11d468713516b14862c4ae4ea68ec5aa30c1475d7d38f17e14585da10ea848a054733f2185b1ea57f10a1c416bb1617baa60";
    private static final String TEMPORARY_FILE_CONTENTS = "A temporary file for unit testing";
    private static final String FILE_IDENTIFIER = "fileIdentifier";
    private static final String TEST_DB_IP = "A temporary file for unit testing";
    private static final Long FILE_SIZE = (long) TEMPORARY_FILE_CONTENTS.length();
    private static final UUID PERSISTED_FILE_ID = UUID.randomUUID();
    private static final int TEST_DB_PORT = 27017;

    @Autowired
    private FileService fileService;
    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private FileMappingRepository fileMappingRepository;
    private PeriodicFilesMarkedForDeletionRemovalTask periodicFilesMarkedForDeletionRemovalTask;
    private MongodExecutable mongodExecutable;

    @BeforeEach
    void setUp() throws Exception {
        setUpEmbeddedMongo();
        periodicFilesMarkedForDeletionRemovalTask = new PeriodicFilesMarkedForDeletionRemovalTask(hazelcastInstance, fileService, 10);
        fileMappingRepository.save(new PersistableFileMapping(PERSISTED_FILE_ID, EPHEMERAL, MONGO_GRID_FS, FILE_IDENTIFIER, SHA_256, SHA_512, FILE_SIZE, false));
        fileMappingRepository.save(new PersistableFileMapping(PERSISTED_FILE_ID, EPHEMERAL, MONGO_GRID_FS, FILE_IDENTIFIER, SHA_256, SHA_512, FILE_SIZE, false));
    }

    void setUpEmbeddedMongo() throws Exception {
        var mongodConfig = new MongodConfigBuilder().version(PRODUCTION)
                .net(new Net(TEST_DB_IP, TEST_DB_PORT, Network.localhostIsIPv6()))
                .build();

        mongodExecutable = MongodStarter.getDefaultInstance().prepare(mongodConfig);
        mongodExecutable.start();
    }

    @AfterEach
    void tearDown() {
        mongodExecutable.stop();
    }

    @Test
    void ephemeralFilesMarkedForDeletionAreDeletedWhenDeleteTaskRuns() {
        fileService.markAllFilesForDeletion();

        periodicFilesMarkedForDeletionRemovalTask.deleteFilesInBatches();

        List<PersistableFileMapping> byMarkedForDeletionTrue = fileMappingRepository.findByMarkedForDeletionTrue(PageRequest.of(0, 10));
        assertTrue(byMarkedForDeletionTrue.isEmpty());
    }
}
