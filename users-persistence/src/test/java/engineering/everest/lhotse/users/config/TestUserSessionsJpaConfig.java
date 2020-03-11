package engineering.everest.lhotse.users.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TestUserSessionsJpaConfig {

    @Bean
    @Primary
    public JpaProperties jpaProperties() {
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.setGenerateDdl(true);
        return jpaProperties;
    }

    @Bean
    public DataSource testDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public SpringLiquibase liquibase(DataSourceProperties dataSourceProperties,
                                     ObjectProvider<DataSource> dataSource,
                                     ObjectProvider<DataSource> liquibaseDataSource) {
        SpringLiquibase springLiquibase = new SpringLiquibase();
        springLiquibase.setShouldRun(false);
        return springLiquibase;
    }
}
