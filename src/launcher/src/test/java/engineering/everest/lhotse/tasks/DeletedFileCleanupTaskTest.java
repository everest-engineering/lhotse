package engineering.everest.lhotse.tasks;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import engineering.everest.starterkit.filestorage.EphemeralDeduplicatingFileStore;
import engineering.everest.starterkit.filestorage.PersistedFileIdentifier;
import engineering.everest.starterkit.filestorage.persistence.FileMappingRepository;
import engineering.everest.starterkit.filestorage.persistence.PersistableFileMapping;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static engineering.everest.starterkit.filestorage.FileStoreType.EPHEMERAL;
import static engineering.everest.starterkit.filestorage.NativeStorageType.MONGO_GRID_FS;
import static java.time.Duration.parse;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DeletedFileCleanupTaskTest {

    private static final UUID FILE_ID = UUID.randomUUID();
    private static final String FILE_IDENTIFIER = "FILE_IDENTIFIER";
    private static final String SHA_256 = "SHA_256";
    private static final String SHA_512 = "SHA_512";
    private static final String TEMPORARY_FILE_CONTENTS = "TEMPORARY_FILE_CONTENTS";
    private static final Long FILE_SIZE = (long) TEMPORARY_FILE_CONTENTS.length();

    private static final PersistableFileMapping PERSISTABLE_FILE_MAPPING =
            new PersistableFileMapping(FILE_ID, EPHEMERAL, MONGO_GRID_FS, FILE_IDENTIFIER, SHA_256, SHA_512, FILE_SIZE, true);

    private DeletedFileCleanupTask deletedFileCleanupTask;

    @Mock
    private EphemeralDeduplicatingFileStore fileStore;
    @Mock
    private FileMappingRepository fileMappingRepository;
    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private CPSubsystem cpSubsystem;
    @Mock
    private FencedLock singleAppNodeExecutionLock;

    @BeforeEach
    void setUp() {
        when(hazelcastInstance.getCPSubsystem()).thenReturn(cpSubsystem);
        when(cpSubsystem.getLock(DeletedFileCleanupTask.class.getSimpleName())).thenReturn(singleAppNodeExecutionLock);
        lenient().when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(true);

        deletedFileCleanupTask = new DeletedFileCleanupTask(fileMappingRepository, fileStore, hazelcastInstance);
    }

    @Test
    void checkForDeletedFilesToCleanUp_IsAnnotatedToRunPeriodically() {
        Method checkForFilesToDeleteMethod = stream(DeletedFileCleanupTask.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("checkForDeletedFilesToCleanUp"))
                .findFirst().orElseThrow();

        Scheduled schedule = checkForFilesToDeleteMethod.getAnnotation(Scheduled.class);
        assertTrue(schedule.fixedRate() > 0 || !schedule.fixedRateString().equals(""));
    }

    @ParameterizedTest
    @MethodSource("exampleTimeStrings")
    void expiryDetectionPeriodCheckRate_WillHandleArbitraryTimeUnits(String input, Duration expectedDuration) {
        Method checkForTimedOutHelpSessionsMethod = stream(DeletedFileCleanupTask.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("checkForDeletedFilesToCleanUp"))
                .findFirst().orElseThrow();
        Scheduled schedule = checkForTimedOutHelpSessionsMethod.getAnnotation(Scheduled.class);

        String expression = schedule.fixedRateString().replace("${storage.files.deletion.fixedRate:5m}", input);
        assertEquals(expectedDuration, parse(expression));
    }

    @Test
    void checkForDeletedFilesToCleanUp_WillHaveNoInteractionsIfThereAreNoFilesToDelete() {
        when(fileMappingRepository.findTop500ByMarkedForDeletionTrue()).thenReturn(emptyList());

        deletedFileCleanupTask.checkForDeletedFilesToCleanUp();

        verify(fileStore).deleteFiles(emptySet());
    }

    @Test
    void checkForDeletedFilesToCleanUp_WillDeleteFilesFromFileStoreIfThereAreFilesMarkedForDeletion() {
        when(fileMappingRepository.findTop500ByMarkedForDeletionTrue()).thenReturn(singletonList(PERSISTABLE_FILE_MAPPING));

        deletedFileCleanupTask.checkForDeletedFilesToCleanUp();

        verify(fileStore).deleteFiles(Set.of(new PersistedFileIdentifier(FILE_ID, EPHEMERAL, MONGO_GRID_FS, FILE_IDENTIFIER)));
    }

    @Test
    void checkForDeletedFilesToCleanUp_WillObtainDistributedExecutionLockWhenLockIsNotAcquired() {
        when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(false);
        when(singleAppNodeExecutionLock.isLocked()).thenReturn(false);

        deletedFileCleanupTask.checkForDeletedFilesToCleanUp();

        verify(singleAppNodeExecutionLock).tryLock(5, SECONDS);
    }

    @Test
    void checkForDeletedFilesToCleanUp_WillNotTryAndObtainLockWhenThisInstanceAlreadyOwnsTheLock() {
        when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(true);

        deletedFileCleanupTask.checkForDeletedFilesToCleanUp();

        verify(singleAppNodeExecutionLock, never()).tryLock(5, SECONDS);
    }

    @Test
    void checkForDeletedFilesToCleanUp_WillDoNothingWhenAnotherInstanceHasAcquiredLock() {
        when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(false);
        when(singleAppNodeExecutionLock.isLocked()).thenReturn(true);

        deletedFileCleanupTask.checkForDeletedFilesToCleanUp();

        verify(singleAppNodeExecutionLock, never()).tryLock(5, SECONDS);
        verifyNoInteractions(fileStore);
    }

    private static Stream<Arguments> exampleTimeStrings() {
        return Stream.of(
                Arguments.of("1s", Duration.ofSeconds(1)),
                Arguments.of("2m", Duration.ofMinutes(2)),
                Arguments.of("3h", Duration.ofHours(3)));
    }
}
