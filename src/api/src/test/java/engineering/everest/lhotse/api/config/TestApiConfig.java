package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.axon.common.services.ReadServiceProvider;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.aopalliance.intercept.MethodInvocation;
import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.KeycloakConfigResolver;
import org.keycloak.adapters.springboot.KeycloakSpringBootConfigResolver;
import org.keycloak.adapters.springsecurity.KeycloakConfiguration;
import org.keycloak.adapters.springsecurity.authentication.KeycloakAuthenticationProvider;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.SimpleAuthorityMapper;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.web.authentication.session.RegisterSessionAuthenticationStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;

@KeycloakConfiguration
@Import(KeycloakSpringBootConfigResolver.class)
@EnableGlobalMethodSecurity(jsr250Enabled = true, prePostEnabled = true)
public class TestApiConfig extends GlobalMethodSecurityConfiguration {

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
    
    @Configuration
    @Order(HIGHEST_PRECEDENCE)
    public static class TestWebSecurityConfig extends KeycloakWebSecurityConfigurerAdapter {

        @Bean
        public KeycloakConfigResolver KeycloakConfigResolver() {
            return new KeycloakSpringBootConfigResolver();
        }

        @Autowired
        public void configureGlobal(AuthenticationManagerBuilder auth) {
            KeycloakAuthenticationProvider keycloakAuthenticationProvider = keycloakAuthenticationProvider();
            keycloakAuthenticationProvider.setGrantedAuthoritiesMapper(new SimpleAuthorityMapper());

            auth.authenticationProvider(keycloakAuthenticationProvider);
        }

        @Override
        protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
            return new RegisterSessionAuthenticationStrategy(new SessionRegistryImpl());
        }

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            super.configure(http);

            http.cors()
                    .and()
                    .csrf()
                    .and()
                    .headers()
                    .cacheControl()
                    .and()
                    .disable()
                    .authorizeRequests()
                    .antMatchers("/api/organizations/**", "/api/version",
                            "/actuator/health/**", "/api/doc/**", "/swagger-ui/**", "/swagger-resources/**", "/sso/login*")
                    .permitAll()
                    .antMatchers("/api/**", "/actuator/prometheus/**").authenticated()
                    .antMatchers("/admin/**", "/actuator/**").hasRole("ADMIN").anyRequest().permitAll()
                    .and()
                    .logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout", "GET")).logoutSuccessUrl("/");
        }
    }

    public class CustomMethodSecurityExpression extends SecurityExpressionRoot
            implements MethodSecurityExpressionOperations {

        private final KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) this.authentication;
        private final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();

        public CustomMethodSecurityExpression(Authentication authentication) {
            super(authentication);
        }

        public boolean hasCustomRole(String role) {
            var otherClaims = principal.getKeycloakSecurityContext().getToken().getOtherClaims();
            return  otherClaims != null && otherClaims.containsKey("roles")
                        && otherClaims.get("roles").toString().trim().contains(role);
        }

        public boolean memberOfOrg(UUID organizationId) {
            var otherClaims = principal.getKeycloakSecurityContext().getToken().getOtherClaims();
            return otherClaims != null && otherClaims.containsKey("organizationId")
                    && organizationId.compareTo(UUID.fromString(otherClaims.get("organizationId").toString())) == 0;
        }

        @Override
        public void setFilterObject(Object o) {
            // Do nothing
        }

        @Override
        public Object getFilterObject() {
            return this;
        }

        @Override
        public void setReturnObject(Object returnObject) {
            // Do nothing
        }

        @Override
        public Object getReturnObject() {
            return this;
        }

        @Override
        public Object getThis() {
            return this;
        }
    }

    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
        return new CustomMethodSecurityExpressionHandler();
    }

    public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
        @Override
        protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication,
                                                                                  MethodInvocation invocation) {
            return new CustomMethodSecurityExpression(authentication);
        }
    }
}

