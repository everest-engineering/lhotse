package engineering.everest.lhotse.axon.replay;

import engineering.everest.lhotse.axon.config.AxonConfig.EventProcessorType;
import org.axonframework.config.Configuration;
import org.axonframework.config.EventProcessingConfigurer.EventProcessorBuilder;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventhandling.DirectEventProcessingStrategy;
import org.axonframework.eventhandling.EventHandlerInvoker;
import org.axonframework.eventhandling.EventProcessor;
import org.axonframework.eventhandling.SubscribingEventProcessor;
import org.axonframework.eventhandling.TrackedEventMessage;
import org.axonframework.eventhandling.TrackingEventProcessor;
import org.axonframework.eventhandling.TrackingEventProcessorConfiguration;
import org.axonframework.messaging.StreamableMessageSource;
import org.springframework.core.task.TaskExecutor;

public class CompositeEventProcessorBuilder implements EventProcessorBuilder {

    private final TaskExecutor taskExecutor;
    private final EventProcessingModule eventProcessingModule;
    private final EventProcessorType eventProcessorType;
    private final int numberOfSegments;

    public CompositeEventProcessorBuilder(TaskExecutor taskExecutor,
                                          EventProcessingModule eventProcessingModule,
                                          EventProcessorType eventProcessorType,
                                          int numberOfSegments) {
        this.taskExecutor = taskExecutor;
        this.eventProcessingModule = eventProcessingModule;
        this.eventProcessorType = eventProcessorType;
        this.numberOfSegments = numberOfSegments;
    }

    @Override
    public EventProcessor build(String name, Configuration configuration, EventHandlerInvoker eventHandlerInvoker) {
        switch (eventProcessorType) {
            case SUBSCRIBING:
                return buildSubscribingEventProcessor(name, configuration, eventHandlerInvoker);
            case TRACKING:
                return buildReplayMarkerAwareTrackingEventProcessor(name, configuration, eventHandlerInvoker);
            default:
                throw new IllegalArgumentException(
                        String.format("Invalid event processor type: %s", eventProcessorType));
        }
    }

    private SubscribingEventProcessor buildSubscribingEventProcessor(
            String name,
            Configuration configuration,
            EventHandlerInvoker eventHandlerInvoker) {
        return SubscribingEventProcessor.builder()
                .name(name)
                .eventHandlerInvoker(eventHandlerInvoker)
                .rollbackConfiguration(eventProcessingModule.rollbackConfiguration(name))
                .messageMonitor(eventProcessingModule.messageMonitor(SubscribingEventProcessor.class, name))
                .messageSource(configuration.eventBus())
                .processingStrategy(DirectEventProcessingStrategy.INSTANCE)
                .transactionManager(eventProcessingModule.transactionManager(name))
                .build();
    }

    @SuppressWarnings("unchecked")
    private ReplayMarkerAwareTrackingEventProcessor buildReplayMarkerAwareTrackingEventProcessor(
            String name,
            Configuration configuration,
            EventHandlerInvoker eventHandlerInvoker) {
        var trackingEventProcessorConfiguration = configuration.getComponent(
                TrackingEventProcessorConfiguration.class,
                () -> TrackingEventProcessorConfiguration.forParallelProcessing(numberOfSegments));

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
    }
}
