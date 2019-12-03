package engineering.everest.starterkit.axon.security.config;

import engineering.everest.starterkit.axon.security.userdetails.AuthServerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Configuration
@EnableAuthorizationServer
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

    private final PasswordEncoder passwordEncoder;
    private final JwtAccessTokenConverter jwtAccessTokenConverter;
    private final AuthServerUserDetailsService authServerUserDetailsService;
    private final AuthenticationManager authenticationManager;
    private final TokenStore tokenStore;
    private final int accessTokenValiditySeconds;
    private final int refreshTokenValiditySeconds;

    @Autowired
    public AuthServerConfig(PasswordEncoder passwordEncoder,
                            JwtAccessTokenConverter jwtAccessTokenConverter,
                            AuthServerUserDetailsService authServerUserDetailsService,
                            AuthenticationManager authenticationManager,
                            @Qualifier("mongoTokenStore") TokenStore tokenStore,
                            @Value("${application.jwt.access-token.validity-seconds:3600}") int accessTokenValiditySeconds,
                            @Value("${application.jwt.refresh-token.validity-seconds:7200}") int refreshTokenValiditySeconds) {
        super();
        this.passwordEncoder = passwordEncoder;
        this.jwtAccessTokenConverter = jwtAccessTokenConverter;
        this.authServerUserDetailsService = authServerUserDetailsService;
        this.authenticationManager = authenticationManager;
        this.tokenStore = tokenStore;
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer securityConfigurer) throws Exception {
        securityConfigurer
                .passwordEncoder(passwordEncoder)
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
                .allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clientsConfigurer) throws Exception {
        clientsConfigurer.inMemory()
                .withClient("web-app-ui")
                .secret(passwordEncoder.encode(""))
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("all")
                .accessTokenValiditySeconds(accessTokenValiditySeconds)
                .refreshTokenValiditySeconds(refreshTokenValiditySeconds)
                .authorities("ROLE_WEB_APP_UI")
                .autoApprove(true);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpointsConfigurer) throws Exception {
        endpointsConfigurer
                .authenticationManager(authenticationManager)
                .accessTokenConverter(jwtAccessTokenConverter)
                .userDetailsService(authServerUserDetailsService)
                .tokenStore(tokenStore);
    }
}
