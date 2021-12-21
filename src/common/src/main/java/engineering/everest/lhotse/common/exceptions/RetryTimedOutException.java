package engineering.everest.lhotse.common.exceptions;

import java.time.Duration;

public class RetryTimedOutException extends Exception {

    public RetryTimedOutException(Duration elapsed, String description) {
        super(String.format("Timed out while waiting %s for '%s'", elapsed, description));
    }
}
