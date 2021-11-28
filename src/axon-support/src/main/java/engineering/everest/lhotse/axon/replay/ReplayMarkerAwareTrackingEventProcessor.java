package engineering.everest.lhotse.axon.replay;

import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.ErrorHandler;
import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.Segment;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.messaging.unitofwork.RollbackConfiguration;
import org.axonframework.monitoring.MessageMonitor;
import org.springframework.core.task.TaskExecutor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

@Slf4j
public class ReplayMarkerAwareTrackingEventProcessor extends TrackingEventProcessor implements ReplayableEventProcessor {

    private final TransactionManager transactionManager;
    private final TokenStore tokenStore;
    private final int initialSegmentsCount;
    private final AtomicReference<ReplayMarkerEvent> targetMarkerEventHolder = new AtomicReference<>();
    private final AtomicInteger workerReplayCompletionCounter = new AtomicInteger();
    private final List<Consumer<ReplayableEventProcessor>> replayCompletionListener = new ArrayList<>();
    private final TaskExecutor taskExecutor;

    protected ReplayMarkerAwareTrackingEventProcessor(Builder builder) {
        super(builder);
        transactionManager = builder.transactionManager;
        tokenStore = builder.tokenStore;
        initialSegmentsCount = builder.initialSegmentsCount;
        taskExecutor = builder.taskExecutor;
    }

    @Override
    public void startReplay(TrackingToken startPosition, ReplayMarkerEvent replayMarkerEvent) {
        synchronized (this) {
            if (isReplaying()) {
                throw new RuntimeException("Previous replay is still running");
            }
            targetMarkerEventHolder.set(replayMarkerEvent);
            workerReplayCompletionCounter.set(0);
            shutDown();
            resetTokens(startPosition);
            start();
        }
    }

    @Override
    public boolean isReplaying() {
        return targetMarkerEventHolder.get() != null;
    }

    @Override
    public ListenerRegistry registerReplayCompletionListener(Consumer<ReplayableEventProcessor> listener) {
        replayCompletionListener.add(listener);
        return () -> replayCompletionListener.remove(listener);
    }

    @Override
    protected boolean canHandle(EventMessage<?> eventMessage, Collection<Segment> segments) throws Exception {
        if (ReplayMarkerEvent.class.isAssignableFrom(eventMessage.getPayloadType())) {
            var targetEvent = targetMarkerEventHolder.get();
            if (targetEvent != null && targetEvent.equals(eventMessage.getPayload())) {
                processReplayMarkerEvent(eventMessage);
            }
        }
        return super.canHandle(eventMessage, segments);
    }

    private void processReplayMarkerEvent(EventMessage<?> eventMessage) {
        LOGGER.info("Processing target replay marker event: {}", eventMessage.getPayload());
        int numberOfActiveSegments = processingStatus().size();
        if (workerReplayCompletionCounter.incrementAndGet() == numberOfActiveSegments) {
            synchronized (this) {
                LOGGER.info("Replay completed: {}", numberOfActiveSegments);
                targetMarkerEventHolder.set(null);
                taskExecutor.execute(() -> List.copyOf(replayCompletionListener).forEach(l -> {
                    try {
                        l.accept(this);
                    } catch (Exception e) {
                        LOGGER.error("Error running replay completion listener", e);
                    }
                }));
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @SuppressWarnings("PMD.AvoidFieldNameMatchingMethodName")
    public static class Builder extends TrackingEventProcessor.Builder {

        private TransactionManager transactionManager;
        private TokenStore tokenStore;
        private int initialSegmentsCount;
        private TaskExecutor taskExecutor;

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

        public Builder taskExecutor(TaskExecutor taskExecutor) {
            this.taskExecutor = taskExecutor;
            return this;
        }

        @Override
        public ReplayMarkerAwareTrackingEventProcessor build() {
            return new ReplayMarkerAwareTrackingEventProcessor(this);
        }
    }
}
