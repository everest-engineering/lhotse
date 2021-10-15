package engineering.everest.lhotse.tasks;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.cp.CPSubsystem;
import com.hazelcast.cp.lock.FencedLock;
import engineering.everest.starterkit.filestorage.FileService;
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
import java.util.stream.Stream;

import static java.time.Duration.parse;
import static java.util.Arrays.stream;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PeriodicFilesMarkedForDeletionRemovalTaskTest {

    private static final int BATCH_SIZE = 50;

    private PeriodicFilesMarkedForDeletionRemovalTask periodicFilesMarkedForDeletionRemovalTask;

    @Mock
    private FileService fileService;
    @Mock
    private HazelcastInstance hazelcastInstance;
    @Mock
    private CPSubsystem cpSubsystem;
    @Mock
    private FencedLock singleAppNodeExecutionLock;

    @BeforeEach
    void setUp() {
        when(hazelcastInstance.getCPSubsystem()).thenReturn(cpSubsystem);
        when(cpSubsystem.getLock(PeriodicFilesMarkedForDeletionRemovalTask.class.getSimpleName())).thenReturn(singleAppNodeExecutionLock);
        lenient().when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(true);

        periodicFilesMarkedForDeletionRemovalTask = new PeriodicFilesMarkedForDeletionRemovalTask(hazelcastInstance, fileService, BATCH_SIZE);
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_IsAnnotatedToRunPeriodically() {
        Method checkForFilesToDeleteMethod = stream(PeriodicFilesMarkedForDeletionRemovalTask.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("checkForFilesMarkedForDeletionToCleanUp"))
                .findFirst().orElseThrow();

        Scheduled schedule = checkForFilesToDeleteMethod.getAnnotation(Scheduled.class);
        assertTrue(schedule.fixedRate() > 0 || !schedule.fixedRateString().equals(""));
    }

    @ParameterizedTest
    @MethodSource("exampleTimeStrings")
    void expiryDetectionPeriodCheckRate_WillHandleArbitraryTimeUnits(String input, Duration expectedDuration) {
        Method checkForTimedOutHelpSessionsMethod = stream(PeriodicFilesMarkedForDeletionRemovalTask.class.getDeclaredMethods())
                .filter(method -> method.getName().equals("checkForFilesMarkedForDeletionToCleanUp"))
                .findFirst().orElseThrow();
        Scheduled schedule = checkForTimedOutHelpSessionsMethod.getAnnotation(Scheduled.class);

        String expression = schedule.fixedRateString().replace("${storage.files.deletion.fixedRate:5m}", input);
        assertEquals(expectedDuration, parse(expression));
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_WillDelegateToFileServiceForDeletion() {
        periodicFilesMarkedForDeletionRemovalTask.checkForFilesMarkedForDeletionToCleanUp();

        verify(fileService).deleteFileBatch(BATCH_SIZE);
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_WillObtainDistributedExecutionLockWhenLockIsNotAcquired() {
        when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(false);
        when(singleAppNodeExecutionLock.isLocked()).thenReturn(false);

        periodicFilesMarkedForDeletionRemovalTask.checkForFilesMarkedForDeletionToCleanUp();

        verify(singleAppNodeExecutionLock).tryLock(5, SECONDS);
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_WillNotTryAndObtainLockWhenThisInstanceAlreadyOwnsTheLock() {
        when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(true);

        periodicFilesMarkedForDeletionRemovalTask.checkForFilesMarkedForDeletionToCleanUp();

        verify(singleAppNodeExecutionLock, never()).tryLock(5, SECONDS);
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_WillDoNothingWhenAnotherInstanceHasAcquiredLock() {
        when(singleAppNodeExecutionLock.isLockedByCurrentThread()).thenReturn(false);
        when(singleAppNodeExecutionLock.isLocked()).thenReturn(true);

        periodicFilesMarkedForDeletionRemovalTask.checkForFilesMarkedForDeletionToCleanUp();

        verify(singleAppNodeExecutionLock, never()).tryLock(5, SECONDS);
        verifyNoInteractions(fileService);
    }

    @Test
    void replayCompleted_WillDelegateToFileServiceToMarkAllFilesForDeletion() {
        periodicFilesMarkedForDeletionRemovalTask.replayCompleted();

        verify(fileService).markAllFilesForDeletion();
    }

    private static Stream<Arguments> exampleTimeStrings() {
        return Stream.of(
                Arguments.of("1s", Duration.ofSeconds(1)),
                Arguments.of("2m", Duration.ofMinutes(2)),
                Arguments.of("3h", Duration.ofHours(3)));
    }
}
