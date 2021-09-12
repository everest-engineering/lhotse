package engineering.everest.lhotse.axon.common;

import java.time.Duration;

public interface Sleeper {
    void sleep(Duration milliseconds) throws InterruptedException;
}
