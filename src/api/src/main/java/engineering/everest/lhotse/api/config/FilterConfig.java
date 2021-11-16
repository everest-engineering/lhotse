package engineering.everest.lhotse.api.config;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.axon.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.organizations.domain.commands.CreateSelfRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.services.UsersReadService;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import static java.util.UUID.randomUUID;

@Slf4j
@Component
public class FilterConfig extends OncePerRequestFilter {
    private static final String ORGANIZATION_STREET = "street";
    private static final String ORGANIZATION_CITY = "city";
    private static final String ORGANIZATION_STATE = "state";
    private static final String ORGANIZATION_COUNTRY = "country";
    private static final String ORGANIZATION_POSTAL_CODE = "postal";
    private static final String ORGANIZATION_WEBSITE_URL = "website-url";
    private static final String ORGANIZATION_CONTACT_PHONE_NUMBER = "0000000000";

    private static final String ORGANIZATION_ID_KEY = "organizationId";
    private static final String DISPLAY_NAME_KEY = "displayName";

    @Autowired
    private HazelcastCommandGateway commandGateway;

    @Autowired
    private UsersReadService usersReadService;

    @Autowired
    private OrganizationsReadService organizationsReadService;

    @SuppressWarnings("unchecked")
    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        var requestUri = httpServletRequest.getRequestURI();
        LOGGER.info("Filtering request: " + requestUri);

        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) httpServletRequest
                .getUserPrincipal();
        var accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();
        try {
            var otherClaims = accessToken.getOtherClaims();
            LOGGER.info("Other claims: " + otherClaims);

            if (otherClaims.containsKey(ORGANIZATION_ID_KEY)) {
                Set<Role> roles = new HashSet<>();
                var rolesObject = otherClaims.get("roles");
                if (rolesObject instanceof Set) {
                    roles.addAll((Set<Role>) rolesObject);
                } else {
                    roles.addAll((ArrayList<Role>) rolesObject);
                }

                LOGGER.info("Already registered user details: " + new User(UUID.fromString(accessToken.getSubject()),
                        UUID.fromString(otherClaims.get(ORGANIZATION_ID_KEY).toString()),
                        accessToken.getPreferredUsername(), otherClaims.get(DISPLAY_NAME_KEY).toString(),
                        accessToken.getEmail(), false, roles));
            } else {
                var organizationId = randomUUID();
                var registeringUserId = UUID.fromString(accessToken.getSubject());
                var userEmailAddress = accessToken.getEmail();
                var organizationName = accessToken.getPreferredUsername();
                var displayName = otherClaims.getOrDefault(DISPLAY_NAME_KEY, "Guest").toString().trim();

                commandGateway.send(new CreateSelfRegisteredOrganizationCommand(organizationId, registeringUserId,
                        organizationName, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                        ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ORGANIZATION_WEBSITE_URL, displayName,
                        ORGANIZATION_CONTACT_PHONE_NUMBER, userEmailAddress));

                Callable<Boolean> projectionsDone = () -> usersReadService.exists(registeringUserId)
                        && organizationsReadService.exists(organizationId)
                        && usersReadService.getById(registeringUserId).getRoles()
                        .contains(Role.ORG_ADMIN);

                RetryWithExponentialBackoff
                        .oneMinuteWaiter()
                        .waitOrThrow(projectionsDone, "user and organization self registration projection update");

                var user = usersReadService.getById(registeringUserId);
                LOGGER.info("Newly registered user details: " + user);

                // Updating the access token with user claims to avoid token regeneration
                accessToken.getOtherClaims().putAll(Map.of(ORGANIZATION_ID_KEY, organizationId, DISPLAY_NAME_KEY,
                        user.getDisplayName(), "roles", user.getRoles()));
                LOGGER.info("Updated user access token with custom claims.");
            }

        } catch (Exception e) {
            LOGGER.error("doFilterInternal error: ", e);
        } finally {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
        }
    }

    // We are using this method as shouldFilter by doing noneMatch for provided patterns and request paths.
    // That means, doFilterInternal checks can be applied to only specified includeUrlPatterns.
    @Override
    protected boolean shouldNotFilter(HttpServletRequest httpServletRequest) {
        final AntPathMatcher pathMatcher = new AntPathMatcher();

        return Stream.of("/api/user/**", "/api/users/**", "/api/organizations/**", "/admin/**")
                .noneMatch(pattern -> pathMatcher.match(pattern, httpServletRequest.getServletPath()));
    }
}
