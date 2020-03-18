package engineering.everest.lhotse.users.config;

import com.zaxxer.hikari.HikariConfig;
import liquibase.integration.spring.SpringLiquibase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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
        entityManagerFactoryRef = "sessionsEntityManagerFactory",
        transactionManagerRef = "sessionsPlatformTransactionManager",
        basePackages = "engineering.everest.starterkit.security"
)
@EnableTransactionManagement
public class UserSessionsJpaConfig {

    private static final String SESSIONS_AUTO_CONFIG_QUALIFIER = "sessions";

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = SESSIONS_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public DataSource sessionsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = SESSIONS_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = SESSIONS_AUTO_CONFIG_QUALIFIER + ".jpa")
    public JpaProperties sessionsJpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    public LocalContainerEntityManagerFactoryBean sessionsEntityManagerFactory(
            EntityManagerFactoryBuilder builder,
            @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
            @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER) JpaProperties jpaProperties) {
        return builder
                .dataSource(dataSource)
                .properties(jpaProperties.getProperties())
                .packages("engineering.everest.starterkit.security.persistence")
                .persistenceUnit(SESSIONS_AUTO_CONFIG_QUALIFIER)
                .build();
    }

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    public PlatformTransactionManager sessionsPlatformTransactionManager(
            @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    public EntityManager sessionsSharedEntityManager(
            @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @ConfigurationProperties(prefix = SESSIONS_AUTO_CONFIG_QUALIFIER + ".liquibase")
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    public LiquibaseProperties sessionsLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER)
    public SpringLiquibase sessionsLiquibase(@Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
                                             @Qualifier(SESSIONS_AUTO_CONFIG_QUALIFIER) LiquibaseProperties properties) {
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