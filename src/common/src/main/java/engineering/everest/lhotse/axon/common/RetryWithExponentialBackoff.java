package engineering.everest.lhotse.axon.common;

import engineering.everest.lhotse.axon.common.exceptions.RetryTimedOutException;
import lombok.extern.log4j.Log4j2;

import java.time.Duration;
import java.util.concurrent.Callable;

import static java.time.Duration.ZERO;

@Log4j2
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

    private Duration findNextSleepDurationNotExceedingMaxDuration(Duration elapsed, Duration currentSleepDuration) {
        var nextSleepDuration = currentSleepDuration.multipliedBy(backoffMultiplier);
        if (nextSleepDuration.plus(elapsed).compareTo(maxDuration) > 0) {
            nextSleepDuration = maxDuration.minus(elapsed);
        }
        return nextSleepDuration;
    }
}
