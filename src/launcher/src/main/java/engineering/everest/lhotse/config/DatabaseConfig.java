package engineering.everest.lhotse.config;

import com.zaxxer.hikari.HikariConfig;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
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
@EnableJpaRepositories(
    entityManagerFactoryRef = "lhotseEntityManagerFactory",
    transactionManagerRef = "lhotsePlatformTransactionManager",
    basePackages = { "engineering.everest", "org.axonframework" })
@EnableTransactionManagement
public class DatabaseConfig {

    @Bean
    @ConfigurationProperties(prefix = "lhotse.datasource.hikari")
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @ConfigurationProperties(prefix = "lhotse.datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @ConfigurationProperties(prefix = "lhotse.jpa")
    public JpaProperties jpaProperties() {
        return new JpaProperties();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean lhotseEntityManagerFactory(EntityManagerFactoryBuilder builder,
                                                                             DataSource dataSource,
                                                                             JpaProperties jpaProperties) {
        return builder
            .dataSource(dataSource)
            .properties(jpaProperties.getProperties())
            .packages("engineering.everest", "org.axonframework")
            .build();
    }

    @Bean
    public PlatformTransactionManager lhotsePlatformTransactionManager(EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    public EntityManager sharedEntityManager(EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @ConfigurationProperties(prefix = "lhotse.liquibase")
    public LiquibaseProperties liquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    public SpringLiquibase springLiquibase(DataSource dataSource, LiquibaseProperties properties) {
        var liquibase = new SpringLiquibase();
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
