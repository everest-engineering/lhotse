package engineering.everest.lhotse.axon.replay;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.lifecycle.ShutdownHandler;
import org.axonframework.lifecycle.StartHandler;
import org.axonframework.messaging.MessageHandlerInterceptor;

import java.util.List;

import static org.axonframework.lifecycle.Phase.LOCAL_MESSAGE_HANDLER_REGISTRATIONS;

@Slf4j
public class SwitchingEventProcessor implements ReplayableEventProcessor {

    private final SubscribingEventProcessor subscribingEventProcessor;
    private final MarkerAwareTrackingEventProcessor trackingEventProcessor;

    private EventProcessor currentEventProcessor;

    public SwitchingEventProcessor(SubscribingEventProcessor subscribingEventProcessor,
                                   MarkerAwareTrackingEventProcessor trackingEventProcessor) {
        this.subscribingEventProcessor = subscribingEventProcessor;
        this.trackingEventProcessor = trackingEventProcessor;
        this.currentEventProcessor = subscribingEventProcessor;
        this.trackingEventProcessor.registerReplayCompletionListener(this::stopReplay);
    }

    @Override
    public void startReplay(TrackingToken trackingToken, ReplayMarkerEvent replayMarkerEvent) {
        synchronized (this) {
            if (isReplaying()) {
                throw new IllegalStateException("Cannot start replay while previous replay is still ongoing");
            }
            LOGGER.info("Starting replay and switching to {}", trackingEventProcessor.getClass().getSimpleName());
            currentEventProcessor.shutDown();
            currentEventProcessor = trackingEventProcessor;
            trackingEventProcessor.startReplay(trackingToken, replayMarkerEvent);
            start();
            LOGGER.info("Started replay");
        }
    }

    public void stopReplay() {
        synchronized (this) {
            if (!isReplaying()) {
                throw new IllegalStateException("Received request to stop replay but not currently in replay mode");
            }
            LOGGER.info("Stopping replay and switching to {}", subscribingEventProcessor.getClass().getSimpleName());
            currentEventProcessor.shutDown();
            LOGGER.info("shutdown completed");
            currentEventProcessor = subscribingEventProcessor;
            start();
            LOGGER.info("Stopped replay");
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    @Override
    public boolean isReplaying() {
        return currentEventProcessor == trackingEventProcessor;
    }

    @Override
    public String getName() {
        return currentEventProcessor.getName();
    }

    @Override
    public List<MessageHandlerInterceptor<? super EventMessage<?>>> getHandlerInterceptors() {
        return currentEventProcessor.getHandlerInterceptors();
    }

    @Override
    @StartHandler(phase = LOCAL_MESSAGE_HANDLER_REGISTRATIONS)
    public void start() {
        currentEventProcessor.start();
    }

    @Override
    @ShutdownHandler(phase = LOCAL_MESSAGE_HANDLER_REGISTRATIONS)
    public void shutDown() {
        currentEventProcessor.shutDown();
    }

    @Override
    public Registration registerHandlerInterceptor(MessageHandlerInterceptor<? super EventMessage<?>> handlerInterceptor) {
        return currentEventProcessor.registerHandlerInterceptor(handlerInterceptor);
    }
}
