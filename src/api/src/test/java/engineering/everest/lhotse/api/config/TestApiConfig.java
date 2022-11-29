package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

@EnableGlobalMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
public class TestApiConfig extends GlobalMethodSecurityConfiguration {

    @Bean
    public DtoConverter dtoConverter() {
        return new DtoConverter();
    }
}
