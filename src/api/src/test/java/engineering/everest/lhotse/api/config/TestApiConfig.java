package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@EnableMethodSecurity(jsr250Enabled = true)
public class TestApiConfig {

    @Bean
    public DtoConverter dtoConverter() {
        return new DtoConverter();
    }
}
