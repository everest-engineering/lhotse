package engineering.everest.lhotse.axon.replay;

import org.axonframework.common.AxonConfigurationException;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.ErrorHandler;
import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.EventMessage;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.eventhandling.TrackingToken;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.messaging.Message;
import org.axonframework.messaging.StreamableMessageSource;
import org.axonframework.messaging.unitofwork.RollbackConfiguration;
import org.axonframework.monitoring.MessageMonitor;
import org.axonframework.monitoring.NoOpMessageMonitorCallback;

public class FlexibleEventProcessor extends TrackingEventProcessor implements MessageMonitor {

    private final TransactionManager transactionManager;
    private final TokenStore tokenStore;
    private final int initialSegmentCount;

    protected FlexibleEventProcessor(Builder builder) {
        super(builder);
        transactionManager = builder.transactionManager;
        tokenStore = builder.tokenStore;
        initialSegmentCount = builder.initialSegmentCount;
    }

    @Override
    public void resetTokens(TrackingToken startPosition) {
        super.resetTokens(startPosition);
    }

    @Override
    protected void reportIgnored(EventMessage<?> eventMessage) {
        // TODO: process marker here?
        super.reportIgnored(eventMessage);
    }

    @Override
    public MonitorCallback onMessageIngested(Message message) {
        if (ReplayMarkerEvent.class.isAssignableFrom(message.getPayloadType())) {
            // TODO
            return null;
        } else {
            return NoOpMessageMonitorCallback.INSTANCE;
        }
    }

    public static class Builder extends TrackingEventProcessor.Builder {

        private TransactionManager transactionManager;
        private TokenStore tokenStore;
        private int initialSegmentCount;

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
            this.initialSegmentCount = trackingEventProcessorConfiguration.getInitialSegmentsCount();
            return this;
        }

        @Override
        public Builder storingTokensAfterProcessing() {
            super.storingTokensAfterProcessing();
            return this;
        }

        @Override
        protected void validate() throws AxonConfigurationException {
            super.validate();
        }

        public TrackingEventProcessor build() {
            return new FlexibleEventProcessor(this);
        }
    }

}
