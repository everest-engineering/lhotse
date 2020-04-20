package engineering.everest.lhotse.axon.replay;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.Registration;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.lifecycle.ShutdownHandler;
import org.axonframework.lifecycle.StartHandler;
import org.axonframework.messaging.MessageHandlerInterceptor;

import java.util.List;

import static org.axonframework.lifecycle.Phase.LOCAL_MESSAGE_HANDLER_REGISTRATIONS;

@Slf4j
public class SwitchingEventProcessor implements EventProcessor {

    private final SubscribingEventProcessor subscribingEventProcessor;
    private final EverestTrackingEventProcessor trackingEventProcessor;

    private EventProcessor currentEventProcessor;

    public SwitchingEventProcessor(SubscribingEventProcessor subscribingEventProcessor,
                                   EverestTrackingEventProcessor trackingEventProcessor) {
        this.subscribingEventProcessor = subscribingEventProcessor;
        this.trackingEventProcessor = trackingEventProcessor;
        this.currentEventProcessor = subscribingEventProcessor;
    }

    public void startReplay(TrackingToken trackingToken) {
        synchronized (this) {
            LOGGER.info(String.format("Starting replay and switching to %s", TrackingEventProcessor.class.getSimpleName()));
            currentEventProcessor.shutDown();
            currentEventProcessor = trackingEventProcessor;
            trackingEventProcessor.resetTokens(trackingToken);
            start();
            LOGGER.info("Started replay");
        }
    }

    public void stopReplay() {
        synchronized (this) {
            LOGGER.info(String.format("Stopping replay and switching to %s", SubscribingEventProcessor.class.getSimpleName()));
            currentEventProcessor.shutDown();
            currentEventProcessor = subscribingEventProcessor;
            start();
            LOGGER.info("Stopped replay");
        }
    }

    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    public boolean isRelaying() {
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
