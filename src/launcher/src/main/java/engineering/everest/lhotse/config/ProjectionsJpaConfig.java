package engineering.everest.lhotse.config;

import com.zaxxer.hikari.HikariConfig;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.common.jpa.EntityManagerProvider;
import org.axonframework.common.jpa.SimpleEntityManagerProvider;
import org.axonframework.common.transaction.TransactionManager;
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
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Slf4j
@Configuration
@EnableJpaRepositories(basePackages = "engineering.everest.lhotse")
@EnableTransactionManagement
public class ProjectionsJpaConfig {

    private static final String PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME = "projections";

    @Bean
    @Primary
    @ConfigurationProperties(prefix = PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME + ".datasource.hikari")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier(PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME)
    @ConfigurationProperties(prefix = PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME + ".datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME + ".jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
                                                                       EntityManagerFactoryBuilder builder,
                                                                       DataSource dataSource,
                                                                       JpaProperties jpaProperties) {
        return builder
            .dataSource(dataSource)
            .properties(jpaProperties.getProperties())
            .packages("engineering.everest.lhotse")
            .persistenceUnit(PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME)
            .build();
    }

    @Bean(name = "transactionManager")
    @Primary
    public ChainedTransactionManager platformTransactionManager(
                                                                EntityManagerFactory entityManagerFactory,
                                                                @Qualifier("file-mappings") PlatformTransactionManager fileMappingsTxManager,
                                                                @Qualifier("axon") PlatformTransactionManager eventsTxManager) {

        var projectionsTxManager = new JpaTransactionManager(entityManagerFactory);
        return new ChainedTransactionManager(eventsTxManager, projectionsTxManager, fileMappingsTxManager);
    }

    @Bean
    @Primary
    public EntityManager sharedEntityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @Primary
    public EntityManagerProvider primaryEntityManagerProvider(EntityManager entityManager) {
        return new SimpleEntityManagerProvider(entityManager);
    }

    @Bean
    @Primary
    public TransactionManager primaryTransactionManager(PlatformTransactionManager platformTransactionManager) {
        return new SpringTransactionManager(platformTransactionManager);
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = PROJECTIONS_AUTO_CONFIG_QUALIFIER_NAME + ".liquibase")
    public LiquibaseProperties primaryMappingsLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @Primary
    public SpringLiquibase primaryLiquibase(DataSource dataSource, LiquibaseProperties properties) {
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
