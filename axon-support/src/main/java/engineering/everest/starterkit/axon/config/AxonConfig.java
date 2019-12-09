package engineering.everest.starterkit.axon.config;

import engineering.everest.starterkit.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.starterkit.axon.LoggingMessageHandlerInterceptor;
import engineering.everest.starterkit.axon.replay.SwitchingEventProcessorBuilder;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.SimpleCommandBus;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.commandhandling.gateway.IntervalRetryScheduler;
import org.axonframework.commandhandling.gateway.RetryScheduler;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.config.EventProcessingModule;
import org.axonframework.eventsourcing.eventstore.EmbeddedEventStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.eventsourcing.eventstore.inmemory.InMemoryEventStorageEngine;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.axonframework.messaging.interceptors.CorrelationDataInterceptor;
import org.axonframework.modelling.command.AnnotationCommandTargetResolver;
import org.axonframework.spring.config.AxonConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

@Slf4j
@Configuration
public class AxonConfig {

    @Bean
    public RetryScheduler retryScheduler(@Value("${application.axon.retry.interval-milli-seconds}") int retryInterval,
                                         @Value("${application.axon.retry.max-count}") int retryMaxCount,
                                         @Value("${application.axon.retry.pool-size}") int retryPoolSize) {
        return IntervalRetryScheduler.builder()
                .retryInterval(retryInterval)
                .maxRetryCount(retryMaxCount)
                .retryExecutor(new ScheduledThreadPoolExecutor(retryPoolSize))
                .build();
    }

    @Bean
    public DefaultCommandGateway defaultCommandGateway(CommandBus commandBus,
                                                       RetryScheduler retryScheduler,
                                                       List<MessageDispatchInterceptor<? super CommandMessage<?>>> dispatchInterceptors) {
        return DefaultCommandGateway.builder()
                .commandBus(commandBus)
                .retryScheduler(retryScheduler)
                .dispatchInterceptors(dispatchInterceptors)
                .build();
    }

    @Bean
    public SimpleCommandBus commandBus(TransactionManager txManager,
                                       AxonConfiguration axonConfiguration,
                                       CommandValidatingMessageHandlerInterceptor commandValidatingMessageHandlerInterceptor,
                                       LoggingMessageHandlerInterceptor loggingMessageHandlerInterceptor) {
        var simpleCommandBus = SimpleCommandBus.builder()
                .transactionManager(txManager)
                .messageMonitor(axonConfiguration.messageMonitor(CommandBus.class, "commandBus"))
                .build();
        simpleCommandBus.registerHandlerInterceptor(new CorrelationDataInterceptor<>(axonConfiguration.correlationDataProviders()));
        simpleCommandBus.registerHandlerInterceptor(commandValidatingMessageHandlerInterceptor);
        simpleCommandBus.registerHandlerInterceptor(loggingMessageHandlerInterceptor);
        return simpleCommandBus;
    }

    @Autowired
    public void configure(AxonConfiguration axonConfiguration,
                          EventProcessingModule eventProcessingModule) {
        eventProcessingModule.byDefaultAssignTo("default");
        eventProcessingModule.registerEventProcessorFactory(
                new SwitchingEventProcessorBuilder(axonConfiguration, eventProcessingModule));
    }

    @Bean
    public AnnotationCommandTargetResolver annotationCommandTargetResolver() {
        return AnnotationCommandTargetResolver.builder().build();
    }

    @Configuration
    @Profile("standalone")
    public static class Standalone {
        @Bean
        public EmbeddedEventStore embeddedEventStore(EventStorageEngine storageEngine, AxonConfiguration configuration) {
            return EmbeddedEventStore.builder()
                    .storageEngine(storageEngine)
                    .messageMonitor(configuration.messageMonitor(EventStore.class, "eventStore"))
                    .build();
        }

        @Bean
        public EventStorageEngine ephemeralStorageEngine() {
            return new InMemoryEventStorageEngine();
        }
    }

}
