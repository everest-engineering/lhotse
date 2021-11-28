package engineering.everest.lhotse.config;

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
    entityManagerFactoryRef = "fileMappingsEntityManagerFactory",
    transactionManagerRef = "fileMappingsPlatformTransactionManager",
    basePackages = {
        "engineering.everest.starterkit.filestorage",
        "engineering.everest.starterkit.media.thumbnails" })
@EnableTransactionManagement
public class FileMappingsJpaConfig {

    private static final String FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER = "file-mappings";

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public DataSource fileMappingsDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER + ".datasource.hikari")
    public HikariConfig hikariConfig() {
        return new HikariConfig();
    }

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    @ConfigurationProperties(prefix = FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER + ".jpa")
    public JpaProperties fileMappingsJpaProperties() {
        return new JpaProperties();
    }

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    public LocalContainerEntityManagerFactoryBean fileMappingsEntityManagerFactory(
                                                                                   EntityManagerFactoryBuilder builder,
                                                                                   @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
                                                                                   @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER) JpaProperties jpaProperties) {
        return builder
            .dataSource(dataSource)
            .properties(jpaProperties.getProperties())
            .packages("engineering.everest.starterkit.filestorage",
                "engineering.everest.starterkit.media.thumbnails")
            .persistenceUnit(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
            .build();
    }

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    public PlatformTransactionManager fileMappingsPlatformTransactionManager(
                                                                             @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    public EntityManager fileMappingsSharedEntityManager(
                                                         @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER) EntityManagerFactory entityManagerFactory) {
        return SharedEntityManagerCreator.createSharedEntityManager(entityManagerFactory);
    }

    @Bean
    @ConfigurationProperties(prefix = FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER + ".liquibase")
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    public LiquibaseProperties fileMappingsLiquibaseProperties() {
        return new LiquibaseProperties();
    }

    @Bean
    @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER)
    public SpringLiquibase fileMappingsLiquibase(@Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER) DataSource dataSource,
                                                 @Qualifier(FILE_MAPPINGS_AUTO_CONFIG_QUALIFIER) LiquibaseProperties properties) {
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
