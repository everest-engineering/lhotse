package engineering.everest.starterkit.organizations.domain.config;

import engineering.everest.starterkit.organizations.domain.OrganizationAggregate;
import org.axonframework.common.caching.JCacheAdapter;
import org.axonframework.eventsourcing.CachingEventSourcingRepository;
import org.axonframework.eventsourcing.EventCountSnapshotTriggerDefinition;
import org.axonframework.eventsourcing.GenericAggregateFactory;
import org.axonframework.eventsourcing.Snapshotter;
import org.axonframework.eventsourcing.eventstore.EventStore;
import org.axonframework.messaging.annotation.ParameterResolverFactory;
import org.axonframework.modelling.command.Repository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrganizationRepositoryConfig {

    private static final int SNAPSHOT_EVENT_COUNT_THRESHOLD = 30;

    private final ParameterResolverFactory parameterResolverFactory;

    @Autowired
    public OrganizationRepositoryConfig(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @Bean
    public Repository<OrganizationAggregate> repositoryForOrganization(EventStore eventStore,
                                                                       Snapshotter snapshotter,
                                                                       JCacheAdapter cacheAdapter) {

        return CachingEventSourcingRepository.builder(OrganizationAggregate.class)
                .aggregateFactory(new GenericAggregateFactory<>(OrganizationAggregate.class))
                .parameterResolverFactory(parameterResolverFactory)
                .snapshotTriggerDefinition(new EventCountSnapshotTriggerDefinition(snapshotter, SNAPSHOT_EVENT_COUNT_THRESHOLD))
                .eventStore(eventStore)
                .cache(cacheAdapter)
                .build();
    }
}
