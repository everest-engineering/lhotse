package engineering.everest.lhotse.axon.common;

import engineering.everest.lhotse.axon.common.exceptions.RetryTimedOutException;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.concurrent.Callable;

import static java.time.Duration.ZERO;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Log4j2
public class RetryWithExponentialBackoff {

    private final Duration initialSleep;
    private final long backoffMultiplier;
    private final Duration maxDuration;

    public RetryWithExponentialBackoff(Duration initialSleep, long backoffMultiplier, Duration maxDuration) {
        this.initialSleep = initialSleep;
        this.backoffMultiplier = backoffMultiplier;
        this.maxDuration = maxDuration;
    }

    public void waitOrThrow(Callable<Boolean> callable, String description) throws Exception {
        Duration elapsed = ZERO;
        Duration currentSleepDuration = initialSleep;

        var completed = callable.call();
        while (!completed && elapsed.compareTo(maxDuration) < 0) {
            LOGGER.info("Waiting {} for {}", currentSleepDuration, description);
            MILLISECONDS.sleep(currentSleepDuration.toMillis());
            elapsed = elapsed.plus(currentSleepDuration);
            currentSleepDuration = findNextSleepDurationNotExceedingMaxDuration(elapsed, currentSleepDuration);
            completed = callable.call();
        }
        if (!completed) {
            throw new RetryTimedOutException(elapsed, description);
        }
    }

    private Duration findNextSleepDurationNotExceedingMaxDuration(Duration elapsed, Duration currentSleepDuration) {
        var nextSleepDuration = currentSleepDuration.multipliedBy(backoffMultiplier);
        if (nextSleepDuration.plus(elapsed).compareTo(maxDuration) > 0) {
            nextSleepDuration = maxDuration.minus(elapsed);
        }
        return nextSleepDuration;
    }
}
