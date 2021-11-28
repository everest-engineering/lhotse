package engineering.everest.lhotse.axon.config;

import com.zaxxer.hikari.HikariConfig;
import engineering.everest.axon.cryptoshredding.CryptoShreddingSerializer;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.jdbc.PersistenceExceptionResolver;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
import org.axonframework.eventhandling.tokenstore.TokenStore;
import org.axonframework.eventhandling.tokenstore.jpa.JpaTokenStore;
import org.axonframework.eventsourcing.eventstore.EventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.JpaEventStorageEngine;
import org.axonframework.eventsourcing.eventstore.jpa.SQLErrorCodesResolver;
import org.axonframework.modelling.saga.repository.SagaStore;
import org.axonframework.modelling.saga.repository.jpa.JpaSagaStore;
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
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
@EnableJpaRepositories(
    entityManagerFactoryRef = "axonEntityManagerFactory",
    transactionManagerRef = "axonPlatformTransactionManager",
    basePackages = { "engineering.everest.axon" })
public class AxonEventStoreConfig {

    static final String AXON_AUTO_CONFIG_QUALIFIER = "axon";

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = AXON_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public DataSource axonDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = AXON_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = AXON_AUTO_CONFIG_QUALIFIER + ".jpa")
    public JpaProperties axonJpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public LocalContainerEntityManagerFactoryBean axonEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                           @Qualifier(AXON_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
                                                                           @Qualifier(AXON_AUTO_CONFIG_QUALIFIER) JpaProperties jpaProperties) {
        return builder
            .dataSource(dataSource)
            .properties(jpaProperties.getProperties())
            .packages("org.axonframework.eventsourcing.eventstore.jpa",
                "org.axonframework.eventhandling.tokenstore.jpa",
                "org.axonframework.modelling.saga.repository.jpa",
                "engineering.everest.axon.cryptoshredding")
            .persistenceUnit(AXON_AUTO_CONFIG_QUALIFIER)
            .build();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public PlatformTransactionManager axonPlatformTransactionManager(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public EntityManager axonSharedEntityManager(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public EntityManagerProvider axonEntityManagerProvider(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) EntityManager entityManager) {
        return new SimpleEntityManagerProvider(entityManager);
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public TransactionManager axonTransactionManager(ChainedTransactionManager transactionManager) {
        return new SpringTransactionManager(transactionManager);
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public PersistenceExceptionResolver axonPersistenceExceptionResolver(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) DataSource dataSource)
        throws SQLException {
        return new SQLErrorCodesResolver(dataSource);
    }

    @Bean
    @Primary
    public EventStorageEngine eventsStorageEngine(Serializer defaultSerializer,
                                                  PersistenceExceptionResolver persistenceExceptionResolver,
                                                  AxonConfiguration configuration,
                                                  CryptoShreddingSerializer cryptoShreddingEventSerializer,
                                                  @Qualifier(AXON_AUTO_CONFIG_QUALIFIER) EntityManagerProvider entityManagerProvider,
                                                  @Qualifier(AXON_AUTO_CONFIG_QUALIFIER) TransactionManager transactionManager) {
        return JpaEventStorageEngine.builder()
            .snapshotSerializer(defaultSerializer)
            .upcasterChain(configuration.upcasterChain())
            .persistenceExceptionResolver(persistenceExceptionResolver)
            .eventSerializer(cryptoShreddingEventSerializer)
            .entityManagerProvider(entityManagerProvider)
            .transactionManager(transactionManager)
            .build();
    }

    @Bean
    public SagaStore globalSagaStore(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) EntityManagerProvider entityManagerProvider,
                                     CryptoShreddingSerializer cryptoShreddingEventSerializer) {
        return JpaSagaStore.builder()
            .serializer(cryptoShreddingEventSerializer)
            .entityManagerProvider(entityManagerProvider)
            .build();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public TokenStore tokenStore(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) EntityManagerProvider entityManagerProvider,
                                 Serializer defaultSerializer) {

        return JpaTokenStore.builder()
            .entityManagerProvider(entityManagerProvider)
            .serializer(defaultSerializer)
            .nodeId(ManagementFactory.getRuntimeMXBean().getName())
            .build();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = AXON_AUTO_CONFIG_QUALIFIER + ".liquibase")
    public LiquibaseProperties eventsLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @Qualifier(AXON_AUTO_CONFIG_QUALIFIER)
    public SpringLiquibase eventsLiquibase(@Qualifier(AXON_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
                                           @Qualifier(AXON_AUTO_CONFIG_QUALIFIER) LiquibaseProperties properties) {
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
