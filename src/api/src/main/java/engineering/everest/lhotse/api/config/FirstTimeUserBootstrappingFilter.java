package engineering.everest.lhotse.api.config;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.common.RandomFieldsGenerator;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.organizations.domain.commands.CreateSelfRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.services.UsersReadService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.UUID.fromString;

@Slf4j
@Component
public class FirstTimeUserBootstrappingFilter extends OncePerRequestFilter {

    private static final String ORGANIZATION_ID_KEY = "organizationId";
    private static final String DISPLAY_NAME_KEY = "displayName";
    private static final Set<String> NOT_INCLUDE_ANT_PATTERNS = Set.of(
        "/admin/organizations",
        "/admin/organizations/**",
        "/api/organizations/**",
        "/api/organizations/**/users",
        "/api/user",
        "/api/user/profile-photo",
        "/api/user/profile-photo/thumbnail",
        "/api/users",
        "/api/users/**",
        "/api/users/**/forget",
        "/api/users/**/roles");

    private final HazelcastCommandGateway commandGateway;
    private final UsersReadService usersReadService;
    private final OrganizationsReadService organizationsReadService;
    private final String defaultKeycloakClientId;
    private final RandomFieldsGenerator randomFieldsGenerator;

    public FirstTimeUserBootstrappingFilter(@Value("${keycloak.resource}") String defaultKeycloakClientId,
                                            HazelcastCommandGateway commandGateway,
                                            UsersReadService usersReadService,
                                            OrganizationsReadService organizationsReadService,
                                            RandomFieldsGenerator randomFieldsGenerator) {
        super();
        this.defaultKeycloakClientId = defaultKeycloakClientId;
        this.commandGateway = commandGateway;
        this.usersReadService = usersReadService;
        this.organizationsReadService = organizationsReadService;
        this.randomFieldsGenerator = randomFieldsGenerator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest,
                                    HttpServletResponse httpServletResponse,
                                    FilterChain filterChain)
        throws ServletException, IOException {
        LOGGER.info("Filtering request: {}", httpServletRequest.getRequestURI());
        var keycloakAuthenticationToken = (KeycloakAuthenticationToken) httpServletRequest.getUserPrincipal();
        var accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();

        try {
            var otherClaims = accessToken.getOtherClaims();
            if (!otherClaims.containsKey(ORGANIZATION_ID_KEY)) {
                var userId = fromString(accessToken.getSubject());
                bootstrapMissingNewlyRegisteredUser(accessToken, userId, otherClaims);
                augmentAccessTokenForRecentlyRegisteredUser(accessToken, userId);
            }
            LOGGER.info("Registered user {} with roles {} on organisation {}",
                UUID.fromString(accessToken.getSubject()),
                accessToken.getResourceAccess(defaultKeycloakClientId).getRoles(),
                UUID.fromString(otherClaims.get(ORGANIZATION_ID_KEY).toString()));
            LOGGER.debug("Other claims: {}", otherClaims);
        } catch (Exception e) {
            LOGGER.error("doFilterInternal error: ", e);
        } finally {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    private void bootstrapMissingNewlyRegisteredUser(AccessToken accessToken,
                                                     UUID userId,
                                                     Map<String, Object> otherClaims)
        throws Exception {
        if (!usersReadService.exists(userId)) {
            LOGGER.info("Bootstrapping newly registered user {}", userId);
            var newOrganizationId = randomFieldsGenerator.genRandomUUID();
            var userEmailAddress = accessToken.getEmail();
            var organizationName = "New Organization";
            var displayName = otherClaims.getOrDefault(DISPLAY_NAME_KEY, "Guest").toString().trim();

            // You really want to disconnect the load balancer when doing a replay.... This would fail nicely if the user
            // were being created first, preventing bogus organisations from being created.
            commandGateway.send(new CreateSelfRegisteredOrganizationCommand(newOrganizationId, userId,
                organizationName, null, null, null, null, null, null,
                displayName, null, userEmailAddress));

            RetryWithExponentialBackoff.oneMinuteWaiter().waitOrThrow(
                () -> usersReadService.exists(userId) && organizationsReadService.exists(newOrganizationId),
                "user and organization self registration projection update");
        }
    }

    private void augmentAccessTokenForRecentlyRegisteredUser(AccessToken accessToken, UUID userId) {
        var user = usersReadService.getById(userId);
        LOGGER.info("User {} is recently registered, - augmenting access token with missing fields", userId);
        accessToken.getOtherClaims().putAll(
            Map.of(ORGANIZATION_ID_KEY, user.getOrganizationId(),
                DISPLAY_NAME_KEY, user.getDisplayName()));
        accessToken.getResourceAccess(defaultKeycloakClientId).addRole(Role.ORG_ADMIN.name());
    }

    // We are using this method as shouldFilter by doing noneMatch for provided patterns and request paths.
    // That means, doFilterInternal checks can be applied to only specified includeUrlPatterns.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        var pathMatcher = new AntPathMatcher();
        return NOT_INCLUDE_ANT_PATTERNS.stream()
            .noneMatch(pattern -> pathMatcher.match(pattern, request.getServletPath()));
    }
}
