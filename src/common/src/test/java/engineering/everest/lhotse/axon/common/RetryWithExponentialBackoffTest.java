package engineering.everest.lhotse.axon.common;

import engineering.everest.lhotse.axon.common.exceptions.RetryTimedOutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class RetryWithExponentialBackoffTest {

    @Mock
    private Sleeper sleeper;

    private RetryWithExponentialBackoff retryWithExponentialBackoff;

    @BeforeEach
    void setUp() {
        retryWithExponentialBackoff = new RetryWithExponentialBackoff(Duration.ofSeconds(5), 2L, Duration.ofMinutes(30), sleeper);
    }

    @Test
    void waitAndReturnOrThrow_WillRetryWithBackupAndThrowWhenMaxDurationReached() throws Exception {
        var exceptionThrown = assertThrows(RetryTimedOutException.class,
            () -> retryWithExponentialBackoff.waitAndReturnOrThrow(() -> null, x -> false, "test"));
        assertEquals("Timed out while waiting PT30M for 'test'", exceptionThrown.getMessage());

        verify(sleeper).sleep(Duration.ofSeconds(5));
        verify(sleeper).sleep(Duration.ofSeconds(10));
        verify(sleeper).sleep(Duration.ofSeconds(20));
        verify(sleeper).sleep(Duration.ofSeconds(40));
        verify(sleeper).sleep(Duration.ofSeconds(80));
        verify(sleeper).sleep(Duration.ofSeconds(160));
        verify(sleeper).sleep(Duration.ofSeconds(320));
        verify(sleeper).sleep(Duration.ofSeconds(640));
        verify(sleeper).sleep(Duration.ofSeconds(525));
        verifyNoMoreInteractions(sleeper);
    }

    @Test
    void waitAndReturnOrThrow_WillNotSleeperWhenPredicateReturnsTrue() throws Exception {
        retryWithExponentialBackoff.waitAndReturnOrThrow(() -> null, x -> true, "test");
        verifyNoMoreInteractions(sleeper);
    }

    @Test
    void waitAndReturnOrThrow_WillRetryUntilPredicateReturnsTrue() throws Exception {
        final AtomicInteger countdown = new AtomicInteger(4);

        retryWithExponentialBackoff.waitAndReturnOrThrow(countdown::decrementAndGet, x -> countdown.get() == 0, "test");

        verify(sleeper).sleep(Duration.ofSeconds(5));
        verify(sleeper).sleep(Duration.ofSeconds(10));
        verify(sleeper).sleep(Duration.ofSeconds(20));
        verifyNoMoreInteractions(sleeper);
    }

    @Test
    void waitOrThrow_WillRetryWithBackupAndThrowWhenMaxDurationReached() throws Exception {
        var exceptionThrown = assertThrows(RetryTimedOutException.class,
            () -> retryWithExponentialBackoff.waitOrThrow(() -> false, "test"));
        assertEquals("Timed out while waiting PT30M for 'test'", exceptionThrown.getMessage());

        verify(sleeper).sleep(Duration.ofSeconds(5));
        verify(sleeper).sleep(Duration.ofSeconds(10));
        verify(sleeper).sleep(Duration.ofSeconds(20));
        verify(sleeper).sleep(Duration.ofSeconds(40));
        verify(sleeper).sleep(Duration.ofSeconds(80));
        verify(sleeper).sleep(Duration.ofSeconds(160));
        verify(sleeper).sleep(Duration.ofSeconds(320));
        verify(sleeper).sleep(Duration.ofSeconds(640));
        verify(sleeper).sleep(Duration.ofSeconds(525));
        verifyNoMoreInteractions(sleeper);
    }

    @Test
    void waitOrThrow_WillNotSleeperWhenCallableReturnsSuccess() throws Exception {
        retryWithExponentialBackoff.waitOrThrow(() -> true, "test");
        verifyNoMoreInteractions(sleeper);
    }

    @Test
    void waitOrThrow_WillRetryUntilCallableReturnsSuccess() throws Exception {
        final AtomicInteger countdown = new AtomicInteger(4);

        retryWithExponentialBackoff.waitOrThrow(() -> countdown.decrementAndGet() == 0, "test");

        verify(sleeper).sleep(Duration.ofSeconds(5));
        verify(sleeper).sleep(Duration.ofSeconds(10));
        verify(sleeper).sleep(Duration.ofSeconds(20));
        verifyNoMoreInteractions(sleeper);
    }
}
