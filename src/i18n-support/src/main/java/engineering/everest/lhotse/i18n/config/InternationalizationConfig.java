package engineering.everest.lhotse.i18n.config;

import engineering.everest.lhotse.i18n.TranslationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

@Configuration
public class InternationalizationConfig {

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        return new LocaleChangeInterceptor();
    }

    @Bean
    public TranslationService translationService() {
        return TranslationService.getInstance();
    }
}
