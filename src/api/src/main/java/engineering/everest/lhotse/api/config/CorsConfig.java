package engineering.everest.lhotse.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    private final String[] allowedOrigins;
    private final String[] allowedMethods;

    @SuppressWarnings({ "PMD.ArrayIsStoredDirectly", "PMD.UseVarargs" })
    public CorsConfig(@Value("${application.cors.global.allowed-origins}") String[] allowedOrigins,
                      @Value("${application.cors.global.allowed-methods}") String[] allowedMethods) {
        super();
        this.allowedOrigins = allowedOrigins;
        this.allowedMethods = allowedMethods;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        WebMvcConfigurer.super.addCorsMappings(registry);

        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods(allowedMethods);
    }
}
