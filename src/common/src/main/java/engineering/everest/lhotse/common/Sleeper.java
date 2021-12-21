package engineering.everest.lhotse.common;

import java.time.Duration;

public interface Sleeper {
    void sleep(Duration milliseconds) throws InterruptedException;
}
