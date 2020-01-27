package engineering.everest.lhotse.axon.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;

@Configuration
public class PersistenceConfig {

    @Bean
    public AuthenticationKeyGenerator authenticationKeyGenerator() {
        return new DefaultAuthenticationKeyGenerator();
    }
}
