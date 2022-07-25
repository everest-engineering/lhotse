package engineering.everest.lhotse.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.security.SecureRandom;

@Configuration
public class SecurityConfig {
    private static final int DEFAULT_PASSWORD_ENCODER_STRENGTH = 10;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(DEFAULT_PASSWORD_ENCODER_STRENGTH, new SecureRandom());
    }
}
