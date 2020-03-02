package engineering.everest.lhotse.organizations.services;

import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class TestProjectionsJpaConfig {

    @Bean
    @Primary
    public JpaProperties jpaProperties() {
        JpaProperties jpaProperties = new JpaProperties();
        jpaProperties.setGenerateDdl(true);
        return jpaProperties;
    }
}
