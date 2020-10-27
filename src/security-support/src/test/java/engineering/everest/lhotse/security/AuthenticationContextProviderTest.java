package engineering.everest.lhotse.security;

import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.security.userdetails.AppUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationContextProviderTest {

    private static final String USER_NAME = "foo@example.com";
    private static final User USER = new User(randomUUID(), randomUUID(), USER_NAME, "");
    private static final AppUserDetails ADMIN_USER_DETAILS = new AppUserDetails(USER);

    @Mock
    private SecurityContext securityContext;
    @Mock
    private OAuth2Authentication oAuth2Authentication;

    private AuthenticationContextProvider authenticationContextProvider;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        authenticationContextProvider = new AuthenticationContextProvider();
    }

    @Test
    void willGetUSer() {
        when(oAuth2Authentication.isAuthenticated()).thenReturn(true);
        when(oAuth2Authentication.getPrincipal()).thenReturn(ADMIN_USER_DETAILS);
        when(securityContext.getAuthentication()).thenReturn(oAuth2Authentication);

        assertEquals(USER, authenticationContextProvider.getUser());
    }

    @Test
    void willThrowAuthenticationFailureException_WhenAuthenticationIsNotOfTypeOAuth2Authentication() {
        when(securityContext.getAuthentication()).thenReturn(mock(Authentication.class));
        assertThrows(AuthenticationFailureException.class, () -> authenticationContextProvider.getUser());
    }

    @Test
    void willThrowAuthenticationFailureException_WhenIsNotAuthenticated() {
        when(oAuth2Authentication.isAuthenticated()).thenReturn(false);
        when(securityContext.getAuthentication()).thenReturn(oAuth2Authentication);
        assertThrows(AuthenticationFailureException.class, () -> authenticationContextProvider.getUser());
    }

    @Test
    void willThrowAuthenticationFailureException_WhenPrincipleIsNotOfTypeAdminUserDetails() {
        when(oAuth2Authentication.isAuthenticated()).thenReturn(true);
        when(oAuth2Authentication.getPrincipal()).thenReturn(new Object());
        when(securityContext.getAuthentication()).thenReturn(oAuth2Authentication);
        assertThrows(AuthenticationFailureException.class, () -> authenticationContextProvider.getUser());
    }
}
