package engineering.everest.starterkit.axon.replay;

import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;

@Slf4j
public class SwitchingEventProcessor implements EventProcessor {

    private final SubscribingEventProcessor subscribingEventProcessor;
    private final TrackingEventProcessor trackingEventProcessor;

    @Delegate
    private EventProcessor currentEventProcessor;

    public SwitchingEventProcessor(SubscribingEventProcessor subscribingEventProcessor,
                                   TrackingEventProcessor trackingEventProcessor) {
        this.subscribingEventProcessor = subscribingEventProcessor;
        this.trackingEventProcessor = trackingEventProcessor;
        currentEventProcessor = subscribingEventProcessor;
    }

    public void startReplay(TrackingToken trackingToken) {
        synchronized (this) {
            LOGGER.info(String.format("Starting replay and switching to %s", TrackingEventProcessor.class.getSimpleName()));
            currentEventProcessor.shutDown();
            currentEventProcessor = trackingEventProcessor;
            trackingEventProcessor.resetTokens(trackingToken);
            start();
            LOGGER.info("Done starting replay");
        }
    }

    public void stopReplay() {
        synchronized (this) {
            LOGGER.info(String.format("Stopping replay and switching to %s", SubscribingEventProcessor.class.getSimpleName()));
            currentEventProcessor.shutDown();
            currentEventProcessor = subscribingEventProcessor;
            start();
            LOGGER.info("Done stopping replay");
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean isRelaying() {
        return currentEventProcessor == trackingEventProcessor;
    }
}
