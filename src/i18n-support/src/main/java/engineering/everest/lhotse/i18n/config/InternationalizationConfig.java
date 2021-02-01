package engineering.everest.lhotse.i18n.config;

import engineering.everest.lhotse.i18n.RequestParameterAcceptHeaderLocaleResolver;
import engineering.everest.lhotse.i18n.TranslationService;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

@Configuration
public class InternationalizationConfig implements WebMvcConfigurer {

    @Bean
    public AcceptHeaderLocaleResolver localeResolver(WebMvcProperties webMvcProperties) {
        return new RequestParameterAcceptHeaderLocaleResolver();
    }

    @Bean
    public TranslationService translationService() {
        return TranslationService.getInstance();
    }
}
