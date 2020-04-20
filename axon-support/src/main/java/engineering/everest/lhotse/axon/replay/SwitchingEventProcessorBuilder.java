package engineering.everest.lhotse.axon.replay;

import org.axonframework.common.transaction.TransactionManager;
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
import org.axonframework.spring.config.AxonConfiguration;

public class SwitchingEventProcessorBuilder implements EventProcessorBuilder {

    private final EventProcessorBuilder subscribingEventProcessorBuilder;
    private final EventProcessorBuilder everestTrackingEventProcessorBuilder;

    @SuppressWarnings("unchecked")
    public SwitchingEventProcessorBuilder(AxonConfiguration axonConfiguration,
                                          EventProcessingModule eventProcessingModule) {
        subscribingEventProcessorBuilder = (name, configuration, eventHandlerInvoker) ->
                SubscribingEventProcessor.builder()
                        .name(name)
                        .eventHandlerInvoker(eventHandlerInvoker)
                        .rollbackConfiguration(eventProcessingModule.rollbackConfiguration(name))
                        .messageMonitor(eventProcessingModule.messageMonitor(SubscribingEventProcessor.class, name))
                        .messageSource(axonConfiguration.eventBus())
                        .processingStrategy(DirectEventProcessingStrategy.INSTANCE)
                        .transactionManager(eventProcessingModule.transactionManager(name))
                        .build();

        everestTrackingEventProcessorBuilder = (name, configuration, eventHandlerInvoker) -> {
            TransactionManager transactionManager = eventProcessingModule.transactionManager(name);
            TrackingEventProcessorConfiguration trackingEventProcessorConfiguration = axonConfiguration.getComponent(
                    TrackingEventProcessorConfiguration.class,
                    TrackingEventProcessorConfiguration::forSingleThreadedProcessing);
            TrackingEventProcessor trackingEventProcessor = TrackingEventProcessor.builder()
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

            return new EverestTrackingEventProcessor(
                    trackingEventProcessor,
                    transactionManager,
                    eventProcessingModule.tokenStore(name),
                    trackingEventProcessorConfiguration.getInitialSegmentsCount());
        };

    }

    @Override
    public EventProcessor build(String name, Configuration configuration, EventHandlerInvoker eventHandlerInvoker) {
        return new SwitchingEventProcessor(
                (SubscribingEventProcessor) subscribingEventProcessorBuilder.build(name, configuration, eventHandlerInvoker),
                (EverestTrackingEventProcessor) everestTrackingEventProcessorBuilder.build(name, configuration, eventHandlerInvoker));
    }
}
