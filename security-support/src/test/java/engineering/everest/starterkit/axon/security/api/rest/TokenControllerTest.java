package engineering.everest.starterkit.axon.security.api.rest;

import engineering.everest.starterkit.axon.security.config.AuthServerConfig;
import engineering.everest.starterkit.axon.security.userdetails.AuthServerUserDetailsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
@ContextConfiguration(classes = {TokenController.class, AuthServerConfig.class})
@AutoConfigureMockMvc
@ExtendWith({MockitoExtension.class, SpringExtension.class})
class TokenControllerTest {

    private static final String REFRESH_TOKEN_VALUE = "refresh-token-value";
    private static final String ACCESS_TOKEN_VALUE = "access-token-value";

    @Autowired
    private MockMvc mockMvc;

    @MockBean(name = "mongoTokenStore")
    private TokenStore tokenStore;
    @MockBean
    private PasswordEncoder passwordEncoder;
    @MockBean
    private JwtAccessTokenConverter jwtAccessTokenConverter;
    @MockBean
    private AuthServerUserDetailsService authServerUserDetailsService;
    @MockBean
    private AuthenticationManager authenticationManager;

    @Mock
    private OAuth2RefreshToken oAuth2RefreshToken;
    @Mock
    private OAuth2AccessToken oAuth2AccessToken;

    @Test
    @WithAnonymousUser
    void willRevokeRefreshToken() throws Exception {
        when(tokenStore.readRefreshToken(REFRESH_TOKEN_VALUE)).thenReturn(oAuth2RefreshToken);
        mockMvc.perform(post("/tokens/refresh-token/revocation?token={token_value}", REFRESH_TOKEN_VALUE))
                .andExpect(status().isOk());

        verify(tokenStore).removeAccessTokenUsingRefreshToken(oAuth2RefreshToken);
        verify(tokenStore).removeRefreshToken(oAuth2RefreshToken);
    }

    @Test
    @WithAnonymousUser
    void willRevokeAccessToken() throws Exception {
        when(tokenStore.readAccessToken(ACCESS_TOKEN_VALUE)).thenReturn(oAuth2AccessToken);
        when(oAuth2AccessToken.getRefreshToken()).thenReturn(oAuth2RefreshToken);
        mockMvc.perform(post("/tokens/access-token/revocation?token={token_value}", ACCESS_TOKEN_VALUE))
                .andExpect(status().isOk());

        verify(tokenStore).removeAccessToken(oAuth2AccessToken);
        verify(tokenStore).removeRefreshToken(oAuth2RefreshToken);
    }
}
