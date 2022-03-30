package engineering.everest.lhotse.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.config.annotation.CorsRegistration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorsConfigTest {

    private static final String[] ALLOWED_ORIGINS = { "home", "office", "the-barn" };
    private static final String[] ALLOWED_METHODS = { "cycling" };

    private CorsConfig corsConfig;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig(ALLOWED_ORIGINS, ALLOWED_METHODS);
    }

    @Test
    void willRegisterCorsConfiguration() {
        var registry = mock(CorsRegistry.class);
        var corsRegistration = mock(CorsRegistration.class);
        when(registry.addMapping("/**")).thenReturn(corsRegistration);
        when(corsRegistration.allowedOrigins(any())).thenReturn(corsRegistration);

        corsConfig.addCorsMappings(registry);

        verify(corsRegistration).allowedMethods(ALLOWED_METHODS);
        verify(corsRegistration).allowedOrigins(ALLOWED_ORIGINS);
    }
}
