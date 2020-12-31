package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.api.AuthUserArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AuthUserArgumentResolver authUserArgumentResolver;
    private final LocaleChangeInterceptor localeChangeInterceptor;

    @Autowired
    public WebConfig(AuthUserArgumentResolver authUserArgumentResolver, LocaleChangeInterceptor localeChangeInterceptor) {
        this.authUserArgumentResolver = authUserArgumentResolver;
        this.localeChangeInterceptor = localeChangeInterceptor;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor);
    }
}
