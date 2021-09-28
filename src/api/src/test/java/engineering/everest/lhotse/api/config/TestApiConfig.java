package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.api.helpers.MockAuthenticationContextProvider;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.axon.common.services.ReadServiceProvider;
import engineering.everest.lhotse.security.AuthenticationContextProvider;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.util.matcher.AndRequestMatcher;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static engineering.everest.lhotse.axon.common.domain.Role.ADMIN;
import static org.mockito.Mockito.mock;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true, jsr250Enabled = true)
@EnableWebSecurity
@Configuration
public class TestApiConfig {

    @Bean
    public DtoConverter dtoConverter() {
        return new DtoConverter();
    }

    @Bean
    public UsersReadService usersReadService() {
        return mock(UsersReadService.class);
    }

    @Bean
    public ReadServiceProvider readServiceProvider() {
        return mock(ReadServiceProvider.class);
    }

    @Bean
    public EntityPermissionEvaluator entityPermissionEvaluator(
            AuthenticationContextProvider authenticationContextProvider,
            ReadServiceProvider readServiceProvider) {
        return new EntityPermissionEvaluator(authenticationContextProvider, readServiceProvider);
    }

    @Configuration
    public static class TestWebConfig implements WebMvcConfigurer {

        @Bean
        AuthenticationContextProvider authenticationContextProvider() {
            return new MockAuthenticationContextProvider();
        }

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new AuthUserArgumentResolver(authenticationContextProvider()));
        }
    }

    @Configuration
    @Order(HIGHEST_PRECEDENCE)
    public static class TestWebSecurityConfig extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity httpSecurity) throws Exception {

            AndRequestMatcher requestMatcher = new AndRequestMatcher(
                    new AntPathRequestMatcher("/api/**"),
                    new NegatedRequestMatcher(new AntPathRequestMatcher("/api/version")),
                    new NegatedRequestMatcher(new AntPathRequestMatcher("/api/organizations/register")),
                    new NegatedRequestMatcher(new AntPathRequestMatcher("/api/organizations/**/register/**"))
            );

            httpSecurity
                    .cors().and()
                    .csrf().disable()
                    .requestMatcher(requestMatcher)
                    .sessionManagement().sessionCreationPolicy(STATELESS).and()
                    .authorizeRequests()
                    .antMatchers("/admin/**").hasRole(ADMIN.toString())
                    .anyRequest().authenticated();
        }
    }
}
