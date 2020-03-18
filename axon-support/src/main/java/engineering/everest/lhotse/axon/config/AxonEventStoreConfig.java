package engineering.everest.lhotse.axon.config;

import com.zaxxer.hikari.HikariConfig;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.SQLErrorCodesResolver;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.inmemory.InMemorySagaStore;
import org.axonframework.serialization.Serializer;
import org.axonframework.spring.config.AxonConfiguration;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.SharedEntityManagerCreator;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import java.lang.management.ManagementFactory;
import java.sql.SQLException;

@Slf4j
@Configuration
public class AxonEventStoreConfig {

    private static final String EVENT_STORE_AUTO_CONFIG_QUALIFIER = "event-store";

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = EVENT_STORE_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public DataSource eventsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = EVENT_STORE_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = EVENT_STORE_AUTO_CONFIG_QUALIFIER + ".jpa")
    public JpaProperties eventStoreJpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public LocalContainerEntityManagerFactoryBean eventsEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) JpaProperties jpaProperties) {
        return builder
                .dataSource(dataSource)
                .properties(jpaProperties.getProperties())
                .packages("org.axonframework.eventsourcing.eventstore.jpa",
                        "org.axonframework.eventhandling.tokenstore.jpa")
                .persistenceUnit(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
                .build();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public PlatformTransactionManager eventsPlatformTransactionManager(
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public EntityManager eventsSharedEntityManager(
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public EntityManagerProvider eventsEntityManagerProvider(
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) EntityManager entityManager) {
        return new SimpleEntityManagerProvider(entityManager);
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public SpringTransactionManager eventsTransactionManager(ChainedTransactionManager transactionManager) {
        return new SpringTransactionManager(transactionManager);
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public PersistenceExceptionResolver eventsPersistenceExceptionResolver(
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) DataSource dataSource) throws SQLException {
        return new SQLErrorCodesResolver(dataSource);
    }

    @Bean
    @Primary
    public EventStorageEngine eventsStorageEngine(Serializer defaultSerializer,
                                                  PersistenceExceptionResolver persistenceExceptionResolver,
                                                  Serializer eventSerializer,
                                                  AxonConfiguration configuration,
                                                  @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) EntityManagerProvider entityManagerProvider,
                                                  SpringTransactionManager transactionManager) {
        return JpaEventStorageEngine.builder()
                .snapshotSerializer(defaultSerializer)
                .upcasterChain(configuration.upcasterChain())
                .persistenceExceptionResolver(persistenceExceptionResolver)
                .eventSerializer(eventSerializer)
                .entityManagerProvider(entityManagerProvider)
                .transactionManager(transactionManager)
                .build();
    }

    @Bean
    public SagaStore sagaStore() {
        return new InMemorySagaStore();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public TokenStore tokenStore(
            @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) EntityManagerProvider entityManagerProvider,
            Serializer defaultSerializer) {

        return JpaTokenStore.builder()
                .entityManagerProvider(entityManagerProvider)
                .serializer(defaultSerializer)
                .nodeId(ManagementFactory.getRuntimeMXBean().getName())
                .build();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = EVENT_STORE_AUTO_CONFIG_QUALIFIER + ".liquibase")
    public LiquibaseProperties eventsLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER)
    public SpringLiquibase eventsLiquibase(@Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
                                           @Qualifier(EVENT_STORE_AUTO_CONFIG_QUALIFIER) LiquibaseProperties properties) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(properties.getChangeLog());
        liquibase.setContexts(properties.getContexts());
        liquibase.setDefaultSchema(properties.getDefaultSchema());
        liquibase.setDropFirst(properties.isDropFirst());
        liquibase.setShouldRun(properties.isEnabled());
        liquibase.setLabels(properties.getLabels());
        liquibase.setChangeLogParameters(properties.getParameters());
        liquibase.setRollbackFile(properties.getRollbackFile());
        return liquibase;
    }
}
