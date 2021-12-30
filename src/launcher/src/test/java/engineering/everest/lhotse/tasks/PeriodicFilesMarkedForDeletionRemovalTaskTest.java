package engineering.everest.lhotse.tasks;

import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.stream.Stream;

import static java.time.Duration.parse;
import static java.util.Arrays.stream;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PeriodicFilesMarkedForDeletionRemovalTaskTest {

    private static final int BATCH_SIZE = 50;

    private PeriodicFilesMarkedForDeletionRemovalTask periodicFilesMarkedForDeletionRemovalTask;

    @Mock
    private EntityManager entityManager;
    @Mock
    private FileService fileService;
    @Mock
    private ThumbnailService thumbnailService;
    @Mock
    private Query lockQuery;

    @BeforeEach
    void setUp() {
        lenient().when(entityManager.createNativeQuery("SELECT pg_try_advisory_lock(42)")).thenReturn(lockQuery);
        lenient().when(entityManager.createNativeQuery("SELECT pg_advisory_unlock(42)")).thenReturn(mock(Query.class));
        lenient().when(lockQuery.getSingleResult()).thenReturn(Boolean.TRUE);

        periodicFilesMarkedForDeletionRemovalTask =
            new PeriodicFilesMarkedForDeletionRemovalTask(entityManager, fileService, thumbnailService, BATCH_SIZE);
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_IsAnnotatedToRunPeriodically() {
        Method checkForFilesToDeleteMethod = stream(PeriodicFilesMarkedForDeletionRemovalTask.class.getDeclaredMethods())
            .filter(method -> method.getName().equals("checkForFilesMarkedForDeletionToCleanUp"))
            .findFirst().orElseThrow();

        var schedule = checkForFilesToDeleteMethod.getAnnotation(Scheduled.class);
        assertTrue(schedule.fixedRate() > 0 || !schedule.fixedRateString().equals(""));
    }

    @ParameterizedTest
    @MethodSource("exampleTimeStrings")
    void expiryDetectionPeriodCheckRate_WillHandleArbitraryTimeUnits(String input, Duration expectedDuration) {
        Method checkForTimedOutHelpSessionsMethod = stream(PeriodicFilesMarkedForDeletionRemovalTask.class.getDeclaredMethods())
            .filter(method -> method.getName().equals("checkForFilesMarkedForDeletionToCleanUp"))
            .findFirst().orElseThrow();
        var schedule = checkForTimedOutHelpSessionsMethod.getAnnotation(Scheduled.class);

        String expression = schedule.fixedRateString().replace("${storage.files.deletion.fixedRate:5m}", input);
        assertEquals(expectedDuration, parse(expression));
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_WillDelegateToFileServiceForDeletion() {
        periodicFilesMarkedForDeletionRemovalTask.checkForFilesMarkedForDeletionToCleanUp();

        verify(fileService).deleteEphemeralFileBatch(BATCH_SIZE);
    }

    @Test
    void checkForFilesMarkedForDeletionToCleanUp_WillDoNothingWhenAnotherInstanceHasAcquiredLock() {
        when(lockQuery.getSingleResult()).thenReturn(Boolean.FALSE);

        periodicFilesMarkedForDeletionRemovalTask.checkForFilesMarkedForDeletionToCleanUp();

        verifyNoInteractions(thumbnailService, fileService);
    }

    @Test
    void replayCompleted_WillDelegateToFileServiceToMarkAllFilesForDeletion() {
        periodicFilesMarkedForDeletionRemovalTask.replayCompleted();

        verify(fileService).markAllEphemeralFilesForDeletion();
    }

    @Test
    void replayCompleted_WillClearAllThumbnails() {
        periodicFilesMarkedForDeletionRemovalTask.replayCompleted();

        verify(thumbnailService).deleteAllThumbnailMappings();
    }

    private static Stream<Arguments> exampleTimeStrings() {
        return Stream.of(
            Arguments.of("1s", Duration.ofSeconds(1)),
            Arguments.of("2m", Duration.ofMinutes(2)),
            Arguments.of("3h", Duration.ofHours(3)));
    }
}
