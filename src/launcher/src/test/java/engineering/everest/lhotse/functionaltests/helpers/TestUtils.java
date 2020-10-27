package engineering.everest.lhotse.functionaltests.helpers;

import java.util.concurrent.TimeUnit;

public class TestUtils {

    public static void assertOk(Runnable runnable) {
        long sleepTime = 50, maxTime = 10_000;
        while (true) {
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
                runnable.run();
                return;
            } catch (AssertionError e) {
                sleepTime *= 2;
                if (sleepTime > maxTime) {
                    throw e;
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
