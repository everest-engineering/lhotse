package engineering.everest.lhotse.axon.replay;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfiguration;
import org.axonframework.config.EventProcessingConfigurer.EventProcessorBuilder;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.ErrorHandler;
import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.Segment;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.messaging.unitofwork.RollbackConfiguration;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.spring.config.AxonConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@Slf4j
public class MarkerAwareTrackingEventProcessor extends TrackingEventProcessor implements ReplayableEventProcessor {

    private final TransactionManager transactionManager;
    private final TokenStore tokenStore;
    private final int initialSegmentsCount;
    private final boolean switchingAware;
    private volatile ReplayMarkerEvent targetReplayMarkerEvent;
    private final AtomicInteger workerReplayCompletionCounter = new AtomicInteger();
    private final List<Consumer<ReplayableEventProcessor>> replayCompletionListener = new ArrayList<>();
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    protected MarkerAwareTrackingEventProcessor(Builder builder) {
        super(builder);
        transactionManager = builder.transactionManager;
        tokenStore = builder.tokenStore;
        initialSegmentsCount = builder.initialSegmentsCount;
        switchingAware = builder.switchingAware;
    }

    @Override
    public synchronized void startReplay(TrackingToken starPosition, ReplayMarkerEvent replayMarkerEvent) {
        if (isReplaying()) {
            throw new RuntimeException("Previous replay is still running");
        }
        targetReplayMarkerEvent = replayMarkerEvent;
        workerReplayCompletionCounter.set(0);
        shutDown();
        if (switchingAware) {
            ensureCorrectStopPosition();
        }
        resetTokens(starPosition);
        start();
    }

    @Override
    public boolean isReplaying() {
        return targetReplayMarkerEvent != null;
    }

    @Override
    public ListenerRegistry registerReplayCompletionListener(Consumer<ReplayableEventProcessor> listener) {
        replayCompletionListener.add(listener);
        return () -> replayCompletionListener.remove(listener);
    }

    @Override
    protected boolean canHandle(EventMessage<?> eventMessage, Collection<Segment> segments) throws Exception {
        maybeProcessReplayMarkerEvent(eventMessage);
        return super.canHandle(eventMessage, segments);
    }

    private void ensureCorrectStopPosition() {
        TrackingToken headToken = getMessageSource().createHeadToken();
        transactionManager.executeInTransaction(() -> {
            int[] segments = tokenStore.fetchSegments(getName());
            if (segments.length > 0) {
                for (int segment : segments) {
                    tokenStore.storeToken(headToken, getName(), segment);
                }
            } else {
                tokenStore.initializeTokenSegments(getName(), initialSegmentsCount, headToken);
            }
        });
    }

    private void maybeProcessReplayMarkerEvent(EventMessage<?> eventMessage) {
        if (ReplayMarkerEvent.class.isAssignableFrom(eventMessage.getPayloadType())
                && targetReplayMarkerEvent != null && targetReplayMarkerEvent.equals(eventMessage.getPayload())) {
            LOGGER.warn("Processing target replay marker event: {}", eventMessage.getPayload());
            final int numberOfActiveSegments = processingStatus().size();
            if (workerReplayCompletionCounter.incrementAndGet() == numberOfActiveSegments) {
                synchronized (this) {
                    LOGGER.warn("Replay completed: {}", numberOfActiveSegments);
                    targetReplayMarkerEvent = null;
                    executorService.submit(() ->
                            Collections.unmodifiableList(replayCompletionListener).forEach(l -> l.accept(this)));
                }
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends TrackingEventProcessor.Builder {

        private TransactionManager transactionManager;
        private TokenStore tokenStore;
        private int initialSegmentsCount;
        private boolean switchingAware;

        @Override
        public Builder name(String name) {
            super.name(name);
            return this;
        }

        @Override
        public Builder eventHandlerInvoker(EventHandlerInvoker eventHandlerInvoker) {
            super.eventHandlerInvoker(eventHandlerInvoker);
            return this;
        }

        @Override
        public Builder rollbackConfiguration(RollbackConfiguration rollbackConfiguration) {
            super.rollbackConfiguration(rollbackConfiguration);
            return this;
        }

        @Override
        public Builder errorHandler(ErrorHandler errorHandler) {
            super.errorHandler(errorHandler);
            return this;
        }

        @Override
        public Builder messageMonitor(MessageMonitor<? super EventMessage<?>> messageMonitor) {
            super.messageMonitor(messageMonitor);
            return this;
        }

        @Override
        public Builder messageSource(StreamableMessageSource<TrackedEventMessage<?>> messageSource) {
            super.messageSource(messageSource);
            return this;
        }

        @Override
        public Builder tokenStore(TokenStore tokenStore) {
            super.tokenStore(tokenStore);
            this.tokenStore = tokenStore;
            return this;
        }

        @Override
        public Builder transactionManager(TransactionManager transactionManager) {
            super.transactionManager(transactionManager);
            this.transactionManager = transactionManager;
            return this;
        }

        @Override
        public Builder trackingEventProcessorConfiguration(
                TrackingEventProcessorConfiguration trackingEventProcessorConfiguration) {
            super.trackingEventProcessorConfiguration(trackingEventProcessorConfiguration);
            this.initialSegmentsCount = trackingEventProcessorConfiguration.getInitialSegmentsCount();
            return this;
        }

        @Override
        public Builder storingTokensAfterProcessing() {
            super.storingTokensAfterProcessing();
            return this;
        }

        public Builder switchingAware(boolean switchingAware) {
            this.switchingAware = switchingAware;
            return this;
        }

        @Override
        protected void validate() throws AxonConfigurationException {
            super.validate();
        }

        public MarkerAwareTrackingEventProcessor build() {
            return new MarkerAwareTrackingEventProcessor(this);
        }
    }

    public static class MarkerAwareTrackingEventProcessorBuilder implements EventProcessorBuilder {

        private final Configuration axonConfiguration;
        private final EventProcessingConfiguration eventProcessingModule;

        public MarkerAwareTrackingEventProcessorBuilder(AxonConfiguration axonConfiguration,
                                                        EventProcessingModule eventProcessingModule) {
            this.axonConfiguration = axonConfiguration;
            this.eventProcessingModule = eventProcessingModule;
        }

        @SuppressWarnings("unchecked")
        @Override
        public EventProcessor build(String name, Configuration configuration,
                                    EventHandlerInvoker eventHandlerInvoker) {

            TransactionManager transactionManager = eventProcessingModule.transactionManager(name);
            TrackingEventProcessorConfiguration trackingEventProcessorConfiguration = axonConfiguration.getComponent(
                    TrackingEventProcessorConfiguration.class,
                    () -> TrackingEventProcessorConfiguration.forParallelProcessing(2));

            return MarkerAwareTrackingEventProcessor.builder()
                    .name(name)
                    .eventHandlerInvoker(eventHandlerInvoker)
                    .rollbackConfiguration(eventProcessingModule.rollbackConfiguration(name))
                    .errorHandler(eventProcessingModule.errorHandler(name))
                    .messageMonitor(eventProcessingModule.messageMonitor(TrackingEventProcessor.class, name))
                    .messageSource((StreamableMessageSource<TrackedEventMessage<?>>) axonConfiguration.eventBus())
                    .tokenStore(eventProcessingModule.tokenStore(name))
                    .transactionManager(transactionManager)
                    .trackingEventProcessorConfiguration(trackingEventProcessorConfiguration)
                    .build();
        }
    }
}
