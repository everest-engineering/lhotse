package engineering.everest.lhotse.api.config;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.axon.common.RandomFieldsGenerator;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.organizations.domain.commands.CreateSelfRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
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
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FirstTimeUserBootstrappingFilterTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final String USERNAME_AND_ORGANISATION_NAME = "user name";
    private static final String DISPLAY_NAME = "New User";
    private static final String USER_EMAIL_ADDRESS = "tester@example.com";

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
    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        filterConfig = new FirstTimeUserBootstrappingFilter("default-client",
                hazelcastCommandGateway, usersReadService, organizationsReadService, randomFieldsGenerator);
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

    @ParameterizedTest
    @ValueSource(strings = {
            "/admin/organizations",
            "/admin/organizations/906e3a66-c6ee-418f-a411-4905eed31fde",
            "/api/organizations/906e3a66-c6ee-418f-a411-4905eed31fde",
            "/api/organizations/906e3a66-c6ee-418f-a411-4905eed31fde/users",
            "/api/user",
            "/api/user/profile-photo",
            "/api/user/profile-photo/thumbnail",
            "/api/users",
            "/api/users/9af4cb20-7eb7-486a-a30b-55141714f6af",
            "/api/users/9af4cb20-7eb7-486a-a30b-55141714f6af/forget",
            "/api/users/9af4cb20-7eb7-486a-a30b-55141714f6af/roles",
    })
    void shouldNotFilterMethod_WillReturnFalseForProtectedApiPaths(String path) {
        when(httpServletRequest.getServletPath()).thenReturn(path);
        assertFalse(filterConfig.shouldNotFilter(httpServletRequest));
    }

    @Mock
    private UsersReadService usersReadService;

    @Mock
    private OrganizationsReadService organizationsReadService;

    @Test
    void organizationRegistrationSaga_WillNotBeTriggeredForAlreadyRegisteredUser() throws ServletException, IOException {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/users");
        when(httpServletRequest.getUserPrincipal())
                .thenReturn(new KeycloakAuthenticationToken(new OidcKeycloakAccount() {
                    @Override
                    public Principal getPrincipal() {
                        return USER_ID::toString;
                    }

                    @Override
                    public Set<String> getRoles() {
                        return Set.of(Role.ORG_USER.toString(), Role.ORG_ADMIN.toString());
                    }

                    @Override
                    public KeycloakSecurityContext getKeycloakSecurityContext() {
                        var accessToken = createDefaultAccessToken();
                        accessToken.setOtherClaims("organizationId", ORGANIZATION_ID);
                        accessToken.setOtherClaims("displayName", DISPLAY_NAME);
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

    @Test
    void willAugmentAccessTokenForRecentlyRegisteredUsersCallingWithOldAccessToken() throws ServletException, IOException {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/users");
        when(httpServletRequest.getUserPrincipal())
                .thenReturn(new KeycloakAuthenticationToken(new OidcKeycloakAccount() {
                    @Override
                    public Principal getPrincipal() {
                        return USER_ID::toString;
                    }

                    @Override
                    public Set<String> getRoles() {
                        return Set.of(Role.ORG_USER.toString(), Role.ORG_ADMIN.toString());
                    }

                    @Override
                    public KeycloakSecurityContext getKeycloakSecurityContext() {
                        return new KeycloakSecurityContext("", createDefaultAccessToken(), "", new IDToken());
                    }
                }, false));
        when(usersReadService.exists(USER_ID)).thenReturn(true);
        when(usersReadService.getById(USER_ID)).thenReturn(new User(USER_ID, ORGANIZATION_ID, USERNAME_AND_ORGANISATION_NAME, DISPLAY_NAME));

        filterConfig.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(organizationsReadService, never()).exists(any());
        verify(hazelcastCommandGateway, never()).send(any());
        verifyNoMoreInteractions(hazelcastCommandGateway, createSelfRegisteredOrganizationCommand);
    }

    @Test
    void willFullyBootstrapNewlyRegisteredUsers() throws ServletException, IOException {
        when(httpServletRequest.getRequestURI()).thenReturn("/api/users");
        when(httpServletRequest.getUserPrincipal())
                .thenReturn(new KeycloakAuthenticationToken(new OidcKeycloakAccount() {
                    @Override
                    public Principal getPrincipal() {
                        return USER_ID::toString;
                    }

                    @Override
                    public Set<String> getRoles() {
                        return Set.of(Role.ORG_USER.toString(), Role.ORG_ADMIN.toString());
                    }

                    @Override
                    public KeycloakSecurityContext getKeycloakSecurityContext() {
                        return new KeycloakSecurityContext("", createDefaultAccessToken(), "", new IDToken());
                    }
                }, false));
        when(randomFieldsGenerator.genRandomUUID()).thenReturn(ORGANIZATION_ID);
        when(usersReadService.exists(USER_ID)).thenReturn(false).thenReturn(true);
        when(organizationsReadService.exists(ORGANIZATION_ID)).thenReturn(true);
        when(usersReadService.getById(USER_ID)).thenReturn(new User(USER_ID, ORGANIZATION_ID, USERNAME_AND_ORGANISATION_NAME, DISPLAY_NAME));

        filterConfig.doFilterInternal(httpServletRequest, httpServletResponse, filterChain);
        verify(hazelcastCommandGateway).send(new CreateSelfRegisteredOrganizationCommand(ORGANIZATION_ID, USER_ID,
                USERNAME_AND_ORGANISATION_NAME, null, null, null, null, null, null,
                        DISPLAY_NAME, null, USER_EMAIL_ADDRESS));
        verifyNoMoreInteractions(hazelcastCommandGateway, createSelfRegisteredOrganizationCommand);
    }

    private AccessToken createDefaultAccessToken() {
        AccessToken accessToken = new AccessToken();
        accessToken.setOtherClaims("displayName", DISPLAY_NAME);
        accessToken.setSubject(USER_ID.toString());
        accessToken.setPreferredUsername(USERNAME_AND_ORGANISATION_NAME);
        accessToken.setEmail(USER_EMAIL_ADDRESS);
        var defaultAccess = new AccessToken.Access();
        defaultAccess.addRole(Role.ORG_USER.toString());
        accessToken.setResourceAccess(Map.of("default-client", defaultAccess));
        return accessToken;
    }
}
