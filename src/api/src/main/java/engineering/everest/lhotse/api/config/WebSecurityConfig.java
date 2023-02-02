package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.api.security.KeycloakJwtGrantedAuthoritiesConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@EnableWebSecurity
@EnableMethodSecurity
@Configuration
public class WebSecurityConfig {

    @Bean
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http, KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter)
        throws Exception {
        var jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(keycloakJwtGrantedAuthoritiesConverter);

        http.cors().and().csrf().disable()
            .authorizeHttpRequests()
            .requestMatchers(
                "/api/organizations/**",
                "/api/version",
                "/actuator/health",
                "/api/doc/**",
                "/swagger-ui/**",
                "/swagger-resources/**",
                "/sso/login*")
            .permitAll()
            .requestMatchers("/api/**").authenticated()
            .requestMatchers("/actuator/health/**", "/actuator/metrics/**", "/actuator/prometheus").hasAnyRole("ADMIN", "MONITORING")
            .requestMatchers("/actuator/**", "/admin/**").hasRole("ADMIN")
            .anyRequest().permitAll()
            .and().logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")).logoutSuccessUrl("/")
            .and().oauth2ResourceServer(configurer -> configurer.jwt().jwtAuthenticationConverter(jwtAuthenticationConverter));
        return http.build();
    }
}
