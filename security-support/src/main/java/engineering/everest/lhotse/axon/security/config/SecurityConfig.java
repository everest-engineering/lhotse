package engineering.everest.lhotse.axon.security.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

import java.security.SecureRandom;

@Configuration
public class SecurityConfig {

    static final String APP_API = "/api/**";
    static final String VERSION_API = "/api/version";
    static final String GUEST_API = "/api/guest";
    static final String SWAGGER_API_DOCUMENTATION = "/api/doc/**";
    static final String ADMIN_API = "/api/admin/**";
    static final String SPRING_ACTUATOR_API = "/actuator/**";
    static final String SPRING_ACTUATOR_HEALTH_API = "/actuator/health/**";
    static final String SPRING_ACTUATOR_PROM_API = "/actuator/prometheus/**";

    private static final int DEFAULT_PASSWORD_ENCODER_STRENGTH = 10;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(DEFAULT_PASSWORD_ENCODER_STRENGTH, new SecureRandom());
    }

    @Bean
    JwtAccessTokenConverter jwtAccessTokenConverter(@Value("${application.jwt.signing-secret}") String jwtSigningSecret) {
        JwtAccessTokenConverter jwtAccessTokenConverter = new JwtAccessTokenConverter();
        jwtAccessTokenConverter.setSigningKey(jwtSigningSecret);
        return jwtAccessTokenConverter;
    }
}
