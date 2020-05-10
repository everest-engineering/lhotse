package engineering.everest.lhotse.axon.replay;

import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackingToken;

import java.io.Closeable;
import java.util.function.Consumer;

public interface ReplayableEventProcessor extends EventProcessor {
    void startReplay(TrackingToken startPosition, ReplayMarkerEvent replayMarkerEvent);

    boolean isReplaying();

    ListenerRegistry registerReplayCompletionListener(Consumer<ReplayableEventProcessor> listener);

    interface ListenerRegistry extends Closeable {}
}
