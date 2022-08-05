package engineering.everest.lhotse.api.services;

import engineering.everest.lhotse.common.domain.UserAttribute;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.Collections.emptyMap;
import static java.util.UUID.fromString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class KeycloakClient {
    private static final String ADMIN_USER_ROLE = "ADMIN";
    private static final String ATTRIBUTES_KEY = "attributes";
    private static final String AUTHORIZATION = "Authorization";
    private static final String BEARER = "Bearer ";
    private static final String CREDENTIALS_KEY = "credentials";
    private static final String EMAIL_KEY = "email";
    private static final String ENABLED_KEY = "enabled";
    private static final String NAME_KEY = "name";
    private static final String PASSWORD_VALUE = "password";
    private static final String TEMPORARY_KEY = "temporary";
    private static final String TYPE_KEY = "type";
    private static final String USERNAME_KEY = "username";
    private static final String VALUE_KEY = "value";

    private final String keycloakServerAuthUrl;
    private final String keycloakAdminEmailAddress;
    private final String keycloakAdminPassword;
    private final String keycloakMasterRealmAdminClientId;
    private final String keycloakRealm;
    private final int keycloakServerConnectionPoolSize;

    public KeycloakClient(@Value("${keycloak.auth-server-url}") String keycloakServerAuthUrl,
                          @Value("${kc.server.admin-email}") String keycloakAdminEmailAddress,
                          @Value("${kc.server.admin-password}") String keycloakAdminPassword,
                          @Value("${kc.server.master-realm.default.client-id}") String keycloakMasterRealmAdminClientId,
                          @Value("${keycloak.realm}") String keycloakRealm,
                          @Value("${kc.server.connection.pool-size}") int keycloakServerConnectionPoolSize) {
        this.keycloakServerAuthUrl = keycloakServerAuthUrl;
        this.keycloakAdminEmailAddress = keycloakAdminEmailAddress;
        this.keycloakAdminPassword = keycloakAdminPassword;
        this.keycloakMasterRealmAdminClientId = keycloakMasterRealmAdminClientId;
        this.keycloakRealm = keycloakRealm;
        this.keycloakServerConnectionPoolSize = keycloakServerConnectionPoolSize;
    }

    public UUID createNewKeycloakUser(String displayName, String emailAddress, String password) {
        createUser(Map.of(
            EMAIL_KEY, emailAddress,
            ENABLED_KEY, true,
            ATTRIBUTES_KEY, new UserAttribute(displayName),
            CREDENTIALS_KEY,
            List.of(Map.of(TYPE_KEY, PASSWORD_VALUE, VALUE_KEY, password, TEMPORARY_KEY, false))));

        return getUserId(emailAddress);
    }

    public UUID createNewAdminKeycloakUser(String displayName, String emailAddress, String password) {
        var userProperties = Map.of(
            EMAIL_KEY, emailAddress,
            ENABLED_KEY, true,
            ATTRIBUTES_KEY, new UserAttribute(displayName),
            CREDENTIALS_KEY,
            List.of(Map.of(TYPE_KEY, PASSWORD_VALUE, VALUE_KEY, password, TEMPORARY_KEY, false)));
        createUser(userProperties);
        var newUserId = getUserId(emailAddress);
        var adminRoleDefinition = fetchRealmRoleDefinition(ADMIN_USER_ROLE);
        addRealmRoleToUser(newUserId, adminRoleDefinition);
        return newUserId;
    }

    public UUID getUserId(String emailAddress) {
        return fromString(new JSONArray(getUsers(Map.of(USERNAME_KEY, emailAddress)))
            .getJSONObject(0)
            .getString("id"));
    }

    public String getUsers(Map<String, Object> queryFilters) {
        var usersUri = constructUrlPath("/users") + getFilters(queryFilters);
        return webclient(usersUri, GET)
            .retrieve()
            .bodyToMono(String.class)
            .block();
    }

    private void createUser(Map<String, Object> userDetails) {
        try {
            webclient(constructUrlPath("/users"), POST)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(userDetails)
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .block();
        } catch (Exception e) {
            LOGGER.error("Keycloak createUser error: " + e);
        }
    }

    private Keycloak getAdminKeycloakClientInstance() {
        return KeycloakBuilder.builder()
            .serverUrl(keycloakServerAuthUrl)
            .grantType(OAuth2Constants.PASSWORD)
            .realm("master")
            .clientId(keycloakMasterRealmAdminClientId)
            .username(keycloakAdminEmailAddress)
            .password(keycloakAdminPassword)
            .resteasyClient(new ResteasyClientBuilder()
                .connectionPoolSize(keycloakServerConnectionPoolSize).build())
            .build();
    }

    private StringBuilder getFilters(Map<String, Object> queryFilters) {
        var filters = new StringBuilder("?");
        if (!queryFilters.isEmpty()) {
            for (var filter : queryFilters.entrySet()) {
                filters.append(filter.getKey());
                filters.append('=');
                filters.append(filter.getValue());
                filters.append('&');
            }
        }
        return filters;
    }

    private String accessToken() {
        return BEARER + getAdminKeycloakClientInstance()
            .tokenManager()
            .getAccessToken()
            .getToken();
    }

    private WebClient.RequestBodySpec webclient(String url, HttpMethod method) {
        return WebClient.create(url)
            .method(method)
            .header(AUTHORIZATION, accessToken());
    }

    private void addRealmRoleToUser(UUID newUserId, Map<String, String> adminRoleDefinition) {
        webclient(String.format("%s/admin/realms/%s/users/%s/role-mappings/realm", keycloakServerAuthUrl, keycloakRealm, newUserId),
            POST)
                .contentType(APPLICATION_JSON)
                .accept(APPLICATION_JSON)
                .bodyValue(List.of(adminRoleDefinition))
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .block();
    }

    private Map<String, String> fetchRealmRoleDefinition(String targetRealmRole) {
        var realmRoles = new JSONArray(webclient(String.format("%s/admin/realms/%s/roles", keycloakServerAuthUrl, keycloakRealm), GET)
            .retrieve()
            .bodyToMono(String.class)
            .block());

        for (var i = 0; i < realmRoles.length(); i++) {
            var role = realmRoles.getJSONObject(i);
            if (targetRealmRole.equals(role.getString(NAME_KEY))) {
                return Map.of(NAME_KEY, targetRealmRole, "id", role.getString("id"));
            }
        }
        return emptyMap();
    }

    private String constructUrlPath(String path) {
        return String.format("%s/%s/%s", keycloakServerAuthUrl, "/admin/realms/default", path);
    }
}
