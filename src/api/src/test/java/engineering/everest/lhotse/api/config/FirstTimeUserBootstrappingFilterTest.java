package engineering.everest.lhotse.api.config;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.organizations.domain.commands.CreateSelfRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.adapters.OidcKeycloakAccount;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.IDToken;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.Principal;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
public class FirstTimeUserBootstrappingFilterTest {

    private final UUID USER_ID = randomUUID();
    private final UUID ORGANIZATION_ID = randomUUID();

    private FirstTimeUserBootstrappingFilter filterConfig;

    @Mock
    private HttpServletRequest httpServletRequest;
    @Mock
    private HttpServletResponse httpServletResponse;
    @Mock
    private FilterChain filterChain;
    @Mock
    private HazelcastCommandGateway hazelcastCommandGateway;
    @Mock
    private CreateSelfRegisteredOrganizationCommand createSelfRegisteredOrganizationCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterConfig = new FirstTimeUserBootstrappingFilter();
    }

    @Test
    void shouldNotFilterMethod_WillReturnTrueForRootPath() {
        when(httpServletRequest.getServletPath()).thenReturn("/");
        assertTrue(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Test
    void shouldNotFilterMethod_WillReturnTrueForInvalidPath() {
        when(httpServletRequest.getServletPath()).thenReturn("/invalid");
        assertTrue(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Test
    void shouldNotFilterMethod_WillReturnTrueForVersionApiPath() {
        when(httpServletRequest.getServletPath()).thenReturn("/api/version");
        assertTrue(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Test
    void shouldNotFilterMethod_WillReturnFalseForProtectedApiPaths() {
        when(httpServletRequest.getServletPath()).thenReturn("/admin/organizations");
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath())
                .thenReturn(String.format("/admin/organizations/%s", ORGANIZATION_ID));
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath())
                .thenReturn(String.format("/api/organizations/%s", ORGANIZATION_ID));
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath())
                .thenReturn(String.format("/api/organizations/%s/users", ORGANIZATION_ID));
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn("/api/user");
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn("/api/user/profile-photo");
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn("/api/user/profile-photo/thumbnail");
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn("/api/users");
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn(String.format("/api/users/%s", USER_ID));
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn(String.format("/api/users/%s/forget", USER_ID));
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));

        when(httpServletRequest.getServletPath()).thenReturn(String.format("/api/users/%s/roles", USER_ID));
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Mock
    private UsersReadService usersReadService;

    @Mock
    private OrganizationsReadService organizationsReadService;

    @Test
    void organizationRegistrationSagaWillNotBeTriggeredForAlreadyRegisteredUser() throws ServletException, IOException {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/users");
        when(httpServletRequest.getUserPrincipal())
                .thenReturn(new KeycloakAuthenticationToken(new OidcKeycloakAccount() {
                    @Override
                    public Principal getPrincipal() {
                        return () -> randomUUID().toString();
                    }

                    @Override
                    public Set<String> getRoles() {
                        return Set.of(Role.ORG_USER.toString(), Role.ORG_ADMIN.toString());
                    }

                    @Override
                    public KeycloakSecurityContext getKeycloakSecurityContext() {
                        AccessToken accessToken = new AccessToken();
                        accessToken.setOtherClaims("organizationId", randomUUID());
                        accessToken.setOtherClaims("roles", Set.of(Role.ORG_USER, Role.ORG_ADMIN));
                        accessToken.setOtherClaims("displayName", "Guest");
                        accessToken.setSubject(randomUUID().toString());
                        accessToken.setPreferredUsername("tester");
                        accessToken.setEmail("tester@everest.engineering");
                        return new KeycloakSecurityContext("", accessToken, "", new IDToken());
                    }
                }, false));

        filterConfig.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(usersReadService, never()).exists(any());
        verify(organizationsReadService, never()).exists(any());
        verify(usersReadService, never()).getById(any());
        verify(hazelcastCommandGateway, never()).send(any());
        verifyNoMoreInteractions(hazelcastCommandGateway, createSelfRegisteredOrganizationCommand);
    }
}
