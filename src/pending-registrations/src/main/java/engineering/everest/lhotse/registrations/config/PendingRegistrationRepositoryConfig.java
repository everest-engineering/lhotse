package engineering.everest.lhotse.registrations.config;

import engineering.everest.lhotse.registrations.domain.PendingRegistrationAggregate;
import org.axonframework.common.caching.JCacheAdapter;
import org.axonframework.eventsourcing.CachingEventSourcingRepository;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PendingRegistrationRepositoryConfig {

    private final ParameterResolverFactory parameterResolverFactory;

    @Autowired
    public PendingRegistrationRepositoryConfig(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @Bean
    public Repository<PendingRegistrationAggregate> repositoryForPendingRegistration(EventStore eventStore,
                                                                                     JCacheAdapter cacheAdapter) {
        return CachingEventSourcingRepository.builder(PendingRegistrationAggregate.class)
                .aggregateFactory(new GenericAggregateFactory<>(PendingRegistrationAggregate.class))
                .parameterResolverFactory(parameterResolverFactory)
                .eventStore(eventStore)
                .cache(cacheAdapter)
                .build();
    }
}
