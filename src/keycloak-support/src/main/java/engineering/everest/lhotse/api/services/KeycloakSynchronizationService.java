package engineering.everest.lhotse.api.services;

import engineering.everest.lhotse.api.exceptions.KeycloakSynchronizationException;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.UserAttribute;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

import static java.util.UUID.fromString;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.PUT;

@Slf4j
@Component
public class KeycloakSynchronizationService {
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
    private static final String NAME_KEY = "name";
    private static final String VALUE_KEY = "value";

    private final String keycloakServerAuthUrl;
    private final String keycloakAdminUser;
    private final String keycloakAdminPassword;
    private final String keycloakMasterRealmAdminClientId;
    private final String keycloakDefaultRealmDefaultClientId;
    private final int keycloakServerConnectionPoolSize;

    public KeycloakSynchronizationService(@Value("${keycloak.auth-server-url}") String keycloakServerAuthUrl,
                                          @Value("${kc.server.admin-user}") String keycloakAdminUser,
                                          @Value("${kc.server.admin-password}") String keycloakAdminPassword,
                                          @Value("${kc.server.master-realm.default.client-id}") String keycloakMasterRealmAdminClientId,
                                          @Value("${keycloak.resource}") String keycloakDefaultRealmDefaultClientId,
                                          @Value("${kc.server.connection.pool-size}") int keycloakServerConnectionPoolSize) {
        this.keycloakServerAuthUrl = keycloakServerAuthUrl;
        this.keycloakAdminUser = keycloakAdminUser;
        this.keycloakAdminPassword = keycloakAdminPassword;
        this.keycloakMasterRealmAdminClientId = keycloakMasterRealmAdminClientId;
        this.keycloakDefaultRealmDefaultClientId = keycloakDefaultRealmDefaultClientId;
        this.keycloakServerConnectionPoolSize = keycloakServerConnectionPoolSize;
    }

    private Keycloak getAdminKeycloakClientInstance() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakServerAuthUrl)
                .grantType(OAuth2Constants.PASSWORD)
                .realm("master")
                .clientId(keycloakMasterRealmAdminClientId)
                .username(keycloakAdminUser)
                .password(keycloakAdminPassword)
                .resteasyClient(new ResteasyClientBuilder()
                        .connectionPoolSize(keycloakServerConnectionPoolSize).build())
                .build();
    }

    public void updateUserAttributes(UUID userId, Map<String, Object> attributes) {
        webclient(constructUrlPath("/users/%s", userId), PUT)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(attributes)
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .block();
    }

    public void deleteUser(UUID userId) {
        webclient(constructUrlPath("/users/%s", userId), DELETE)
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .block();
    }

    public void createUser(Map<String, Object> userDetails) {
        webclient(constructUrlPath("/users", ""), POST)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(userDetails)
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .block();
    }

    public String getUsers(Map<String, Object> queryFilters) {
        var usersUri = constructUrlPath("/users", "") + getFilters(queryFilters);
        return webclient(usersUri, GET)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getClientDetails(Map<String, Object> queryFilters) {
        var clientsUri = constructUrlPath("/clients", "") + getFilters(queryFilters);
        return webclient(clientsUri, GET)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    public String getClientRoles(String type, UUID userId, UUID clientId) {
        return webclient(constructUrlPath("/users/%s/role-mappings/clients/%s/%s", userId, clientId, type), GET)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    private void updateClientRoles(String path, HttpMethod method, Object data) {
        webclient(path, method)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(data)
                .exchangeToMono(res -> Mono.just(res.statusCode()))
                .block();
    }

    private String getClientRolesPath(UUID userId, UUID clientId) {
        return constructUrlPath("/users/%s/role-mappings/clients/%s", userId, clientId);
    }

    public void addClientLevelUserRoles(UUID userId, Set<Role> roles) {
        var clientId = getClientIdFromClientDetails();
        var availableClientRoles = new JSONArray(getClientRoles("available", userId, clientId));
        for (var i = 0; i < availableClientRoles.length(); i++) {
            var role = availableClientRoles.getJSONObject(i);
            if (roles.contains(Role.valueOf(role.getString(NAME_KEY)))) {
                updateClientRoles(getClientRolesPath(userId, clientId), POST, getClientLevelRolesRequestData(role));
            }
        }
    }

    public void removeClientLevelUserRoles(UUID userId, Set<Role> roles) {
        var clientId = getClientIdFromClientDetails();
        var availableClientRoles = new JSONArray(getClientRoles("", userId, clientId));
        for (var i = 0; i < availableClientRoles.length(); i++) {
            var role = availableClientRoles.getJSONObject(i);
            if (roles.contains(Role.valueOf(role.getString(NAME_KEY)))) {
                updateClientRoles(getClientRolesPath(userId, clientId), DELETE, getClientLevelRolesRequestData(role));
            }
        }
    }

    public String getClientSecret(UUID clientId) {
        return webclient(constructUrlPath("/clients/%s/client-secret", clientId), GET)
                .retrieve()
                .bodyToMono(String.class)
                .block();
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

    public Map<String, Object> setupKeycloakUser(String username, String email, boolean enabled, UUID organizationId,
                                                 Set<Role> roles, String displayName, String password, boolean passwordTemporary) {
        var userDetails = new HashMap<String, Object>();
        try {
            createUser(
                    Map.of("username", username,
                            "email", email,
                            "enabled", enabled,
                            "attributes", new UserAttribute(organizationId, displayName),
                            "credentials",
                            List.of(
                                    Map.of("type", "password",
                                            VALUE_KEY, password,
                                            "temporary", passwordTemporary))));

            var clientSecret = getClientSecret(getClientIdFromClientDetails());
            if (clientSecret.contains(VALUE_KEY)) {
                userDetails.put("clientSecret", new JSONObject(clientSecret).getString(VALUE_KEY));
            }

            var userId = fromString(new JSONArray(getUsers(Map.of("username", username)))
                    .getJSONObject(0).getString("id"));
            userDetails.put("userId", userId);

            addClientLevelUserRoles(userId, roles);
        } catch (Exception e) {
            throw (KeycloakSynchronizationException)new KeycloakSynchronizationException(e.getMessage()).initCause(e);
        }
        return userDetails;
    }

    private UUID getClientIdFromClientDetails() {
        return fromString(new JSONArray(getClientDetails(Map.of("clientId", keycloakDefaultRealmDefaultClientId)))
                .getJSONObject(0).getString("id"));
    }

    private String constructUrlPath(String path, Object...params) {
        return String.format(keycloakServerAuthUrl + "/admin/realms/default" + path, params);
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

    private List<Map<Object, Object>> getClientLevelRolesRequestData(JSONObject role) {
        return List.of(Map.of("id", role.getString("id"),
                NAME_KEY, role.getString(NAME_KEY),
                "description", role.getString("description"),
                "composite", role.getBoolean("composite"),
                "clientRole", role.getBoolean("clientRole"),
                "containerId", role.getString("containerId")));
    }
}
