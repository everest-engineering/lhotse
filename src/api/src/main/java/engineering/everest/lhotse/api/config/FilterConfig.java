package engineering.everest.lhotse.api.config;

import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.IDToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.registrations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.registrations.domain.commands.RegisterOrganizationCommand;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static java.util.UUID.randomUUID;

@Slf4j
@Component
public class FilterConfig extends OncePerRequestFilter {

        private static final String ENCODED_PASSWORD = "encoded-password";
        private static final String ORGANIZATION_STREET = "street";
        private static final String ORGANIZATION_CITY = "city";
        private static final String ORGANIZATION_STATE = "state";
        private static final String ORGANIZATION_COUNTRY = "country";
        private static final String ORGANIZATION_POSTAL_CODE = "postal";
        private static final String ORGANIZATION_WEBSITE_URL = "website-url";
        private static final String ORGANIZATION_CONTACT_PHONE_NUMBER = "0000000000";

        private static final String USER_ID_KEY = "userid";
        private static final String EMAIL_ID_KEY = "email";
        private static final String USERNAME_KEY = "username";
        private static final String USER_DISPLAYNAME_KEY = "displayname";

        @Autowired
        private HazelcastCommandGateway commandGateway;

        @Override
        protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                        FilterChain filterChain) throws ServletException, IOException {
                try {
                        String requestUri = httpServletRequest.getRequestURI();
                        LOGGER.info("Filtering: " + requestUri);

                        KeycloakSecurityContext keycloakSecurityContext = (KeycloakSecurityContext) httpServletRequest
                                        .getAttribute(KeycloakSecurityContext.class.getName());

                        IDToken idToken = keycloakSecurityContext.getIdToken();

                        Map<String, String> claims = new HashMap<>();
                        claims.put(USER_ID_KEY, idToken.getSubject());
                        claims.put(EMAIL_ID_KEY, idToken.getEmail());
                        claims.put(USERNAME_KEY, idToken.getPreferredUsername());

                        Map<String, Object> otherClaims = idToken.getOtherClaims();
                        if(otherClaims.containsKey(USER_DISPLAYNAME_KEY)) {
                                claims.put(USER_DISPLAYNAME_KEY, otherClaims.get(USER_DISPLAYNAME_KEY).toString());
                        } else {
                                throw new RuntimeException(USER_DISPLAYNAME_KEY+" property doesn't exist in the id token.");
                        }

                        LOGGER.info("User info: " + claims.toString());

                        UUID organizationId = randomUUID();
                        UUID registrationConfirmationCode = randomUUID();

                        String organizationName = claims.get(USER_DISPLAYNAME_KEY);
                        if(organizationName.trim().equals("")) {
                                organizationName = claims.get(USERNAME_KEY);
                        }
                        organizationName += "_default";

                        commandGateway.sendAndWait(new RegisterOrganizationCommand(registrationConfirmationCode,
                                        organizationId, UUID.fromString(claims.get(USER_ID_KEY)),
                                        claims.get(EMAIL_ID_KEY), ENCODED_PASSWORD, organizationName,
                                        ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                                        ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ORGANIZATION_WEBSITE_URL,
                                        claims.get(USER_DISPLAYNAME_KEY), ORGANIZATION_CONTACT_PHONE_NUMBER));

                        commandGateway.sendAndWait(new ConfirmOrganizationRegistrationEmailCommand(
                                        registrationConfirmationCode, organizationId));

                } catch (Exception e) {
                        LOGGER.error("doFilterInternal error: ", e);
                } finally {
                        filterChain.doFilter(httpServletRequest, httpServletResponse);
                }

        }

        // We are using this method as shouldFilter by doing noneMatch for provided patterns and request paths.
        // That means, doFilterInternal checks can be applied to only specified includeUrlPatterns.
        @Override
        protected boolean shouldNotFilter(HttpServletRequest httpServletRequest) throws ServletException {
                AntPathMatcher pathMatcher = new AntPathMatcher();

                List<String> includeUrlPatterns = new ArrayList<>();
                includeUrlPatterns.add("/api/user/**");
                includeUrlPatterns.add("/api/users/**");
                includeUrlPatterns.add("/api/organizations/**");
                includeUrlPatterns.add("/admin/**");

                return includeUrlPatterns.stream()
                                .noneMatch(pattern -> pathMatcher.match(pattern, httpServletRequest.getServletPath()));
        }

}
