package engineering.everest.lhotse.axon.replay;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingToken;

public interface ReplayableEventProcessor extends EventProcessor {
    void startReplay(TrackingToken trackingToken, ReplayMarkerEvent replayMarkerEvent);

    boolean isReplaying();
}
