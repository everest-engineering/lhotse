package engineering.everest.lhotse.common;

import engineering.everest.lhotse.common.exceptions.RetryTimedOutException;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static java.time.Duration.ZERO;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Slf4j
public class RetryWithExponentialBackoff {
    private final Duration initialSleep;
    private final long backoffMultiplier;
    private final Duration maxDuration;
    private final Sleeper sleeper;

    public RetryWithExponentialBackoff(Duration initialSleep, long backoffMultiplier, Duration maxDuration, Sleeper sleeper) {
        this.initialSleep = initialSleep;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDuration = maxDuration;
        this.sleeper = sleeper;
    }

    public void waitOrThrow(Callable<Boolean> callable, String description) throws Exception {
        var elapsed = ZERO;
        var currentSleepDuration = initialSleep;

        boolean completed = callable.call();
        while (!completed && elapsed.compareTo(maxDuration) < 0) {
            LOGGER.info("Waiting {} for {}", currentSleepDuration, description);
            sleeper.sleep(currentSleepDuration);
            elapsed = elapsed.plus(currentSleepDuration);
            currentSleepDuration = findNextSleepDurationNotExceedingMaxDuration(elapsed, currentSleepDuration);
            completed = callable.call();
        }
        if (!completed) {
            throw new RetryTimedOutException(elapsed, description);
        }
    }

    public <T> T waitAndReturnOrThrow(Callable<T> callable, Predicate<T> stopPredicate, String description) throws Exception {
        var elapsed = ZERO;
        var currentSleepDuration = initialSleep;

        T callResult = callable.call();
        while (!stopPredicate.test(callResult) && elapsed.compareTo(maxDuration) < 0) {
            LOGGER.info("Waiting {} for {}", currentSleepDuration, description);
            sleeper.sleep(currentSleepDuration);
            elapsed = elapsed.plus(currentSleepDuration);
            currentSleepDuration = findNextSleepDurationNotExceedingMaxDuration(elapsed, currentSleepDuration);
            callResult = callable.call();
        }
        if (stopPredicate.test(callResult)) {
            return callResult;
        }
        throw new RetryTimedOutException(elapsed, description);
    }

    private Duration findNextSleepDurationNotExceedingMaxDuration(Duration elapsed, Duration currentSleepDuration) {
        var nextSleepDuration = currentSleepDuration.multipliedBy(backoffMultiplier);
        if (nextSleepDuration.plus(elapsed).compareTo(maxDuration) > 0) {
            nextSleepDuration = maxDuration.minus(elapsed);
        }
        return nextSleepDuration;
    }

    public static RetryWithExponentialBackoff oneMinuteWaiter() {
        return new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, Duration.ofMinutes(1),
            sleepDuration -> MILLISECONDS.sleep(sleepDuration.toMillis()));
    }

    public static RetryWithExponentialBackoff withMaxDuration(Duration maxDuration) {
        return new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, maxDuration,
            sleepDuration -> MILLISECONDS.sleep(sleepDuration.toMillis()));
    }
}
