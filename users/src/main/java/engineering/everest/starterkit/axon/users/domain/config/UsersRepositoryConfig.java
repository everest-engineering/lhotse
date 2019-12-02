package engineering.everest.starterkit.axon.users.domain.config;

import engineering.everest.starterkit.axon.users.domain.UserAggregate;
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
public class UsersRepositoryConfig {

    private static final int SNAPSHOT_EVENT_COUNT_THRESHOLD = 30;

    private final ParameterResolverFactory parameterResolverFactory;

    @Autowired
    public UsersRepositoryConfig(ParameterResolverFactory parameterResolverFactory) {
        this.parameterResolverFactory = parameterResolverFactory;
    }

    @Bean
    public Repository<UserAggregate> repositoryForUser(EventStore eventStore, Snapshotter snapshotter, JCacheAdapter cacheAdapter) {

        return CachingEventSourcingRepository.builder(UserAggregate.class)
                .aggregateFactory(new GenericAggregateFactory<>(UserAggregate.class))
                .parameterResolverFactory(parameterResolverFactory)
                .snapshotTriggerDefinition(new EventCountSnapshotTriggerDefinition(snapshotter, SNAPSHOT_EVENT_COUNT_THRESHOLD))
                .eventStore(eventStore)
                .cache(cacheAdapter)
                .build();
    }
}
