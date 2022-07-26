package engineering.everest.lhotse.photos.persistence.config;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class TestPhotosJpaConfig {

    @Bean
    @Primary
    public JpaProperties jpaProperties() {
        var jpaProperties = new JpaProperties();
        jpaProperties.setGenerateDdl(true);
        jpaProperties.setShowSql(true);
        return jpaProperties;
    }

    @Bean
    public SpringLiquibase liquibase(DataSourceProperties dataSourceProperties,
                                     ObjectProvider<DataSource> dataSource,
                                     ObjectProvider<DataSource> liquibaseDataSource) {
        var springLiquibase = new SpringLiquibase();
        springLiquibase.setShouldRun(false);
        return springLiquibase;
    }
}
