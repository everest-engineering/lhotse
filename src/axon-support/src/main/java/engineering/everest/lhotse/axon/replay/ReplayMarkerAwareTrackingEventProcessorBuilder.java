package engineering.everest.lhotse.axon.replay;

import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer.EventProcessorBuilder;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.messaging.StreamableMessageSource;
import org.springframework.core.task.TaskExecutor;

public class ReplayMarkerAwareTrackingEventProcessorBuilder implements EventProcessorBuilder {

    private final TaskExecutor taskExecutor;
    private final EventProcessingModule eventProcessingModule;

    public ReplayMarkerAwareTrackingEventProcessorBuilder(TaskExecutor taskExecutor, EventProcessingModule eventProcessingModule) {
        this.taskExecutor = taskExecutor;
        this.eventProcessingModule = eventProcessingModule;
    }

    @Override
    @SuppressWarnings("unchecked")
    public EventProcessor build(String name, Configuration configuration, EventHandlerInvoker eventHandlerInvoker) {
        var trackingEventProcessorConfiguration = configuration.getComponent(
            TrackingEventProcessorConfiguration.class,
            () -> TrackingEventProcessorConfiguration.forParallelProcessing(1));

        if (eventHandlerInvoker.supportsReset()) {
            return ReplayMarkerAwareTrackingEventProcessor.builder()
                .name(name)
                .eventHandlerInvoker(eventHandlerInvoker)
                .rollbackConfiguration(eventProcessingModule.rollbackConfiguration(name))
                .errorHandler(eventProcessingModule.errorHandler(name))
                .messageMonitor(eventProcessingModule.messageMonitor(TrackingEventProcessor.class, name))
                .messageSource((StreamableMessageSource<TrackedEventMessage<?>>) configuration.eventBus())
                .tokenStore(eventProcessingModule.tokenStore(name))
                .transactionManager(eventProcessingModule.transactionManager(name))
                .trackingEventProcessorConfiguration(trackingEventProcessorConfiguration)
                .taskExecutor(taskExecutor)
                .build();
        } else {
            return TrackingEventProcessor.builder()
                .name(name)
                .eventHandlerInvoker(eventHandlerInvoker)
                .rollbackConfiguration(eventProcessingModule.rollbackConfiguration(name))
                .errorHandler(eventProcessingModule.errorHandler(name))
                .messageMonitor(eventProcessingModule.messageMonitor(TrackingEventProcessor.class, name))
                .messageSource((StreamableMessageSource<TrackedEventMessage<?>>) configuration.eventBus())
                .tokenStore(eventProcessingModule.tokenStore(name))
                .transactionManager(eventProcessingModule.transactionManager(name))
                .trackingEventProcessorConfiguration(trackingEventProcessorConfiguration)
                .build();
        }
    }
}
