package engineering.everest.lhotse.axon.common.services;

import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.UserAttribute;
import engineering.everest.lhotse.axon.common.exceptions.KeycloakSynchronizationException;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.json.JSONArray;
import org.json.JSONObject;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;

@Slf4j
@Component
public class KeycloakSynchronizationService {
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";
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
        return KeycloakBuilder.builder().serverUrl(keycloakServerAuthUrl).grantType(OAuth2Constants.PASSWORD)
                .realm("master").clientId(keycloakMasterRealmAdminClientId).username(keycloakAdminUser)
                .password(keycloakAdminPassword)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(keycloakServerConnectionPoolSize).build()).build();
    }

    public void updateUserAttributes(UUID userId, Map<String, Object> attributes) {
        var usersUri = String.format("%s/admin/realms/default/users/%s", keycloakServerAuthUrl, userId);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        WebClient.create(usersUri).put().header(AUTHORIZATION, accessToken).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).bodyValue(attributes)
                .exchangeToMono(res -> Mono.just(res.statusCode())).block();
    }

    public void deleteUser(UUID userId) {
        var usersUri = String.format("%s/admin/realms/default/users/%s", keycloakServerAuthUrl, userId);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        WebClient.create(usersUri).delete().header(AUTHORIZATION, accessToken)
                .exchangeToMono(res -> Mono.just(res.statusCode())).block();
    }

    public void createUser(Map<String, Object> userDetails) {
        var usersUri = String.format("%s/admin/realms/default/users", keycloakServerAuthUrl);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        WebClient.create(usersUri).post().header(AUTHORIZATION, accessToken).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).bodyValue(userDetails)
                .exchangeToMono(res -> Mono.just(res.statusCode())).block();
    }

    public String getUsers(Map<String, Object> queryFilters) {
        var usersUri = String.format("%s/admin/realms/default/users", keycloakServerAuthUrl);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        usersUri += getFilters(queryFilters);
        return WebClient.create(usersUri).get().header(AUTHORIZATION, accessToken).retrieve().bodyToMono(String.class)
                .block();
    }

    public String getClientDetails(Map<String, Object> queryFilters) {
        var clientsUri = String.format("%s/admin/realms/default/clients", keycloakServerAuthUrl);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        clientsUri += getFilters(queryFilters);
        return WebClient.create(clientsUri).get().header(AUTHORIZATION, accessToken).retrieve().bodyToMono(String.class)
                .block();
    }

    public String getClientLevelRoleMappings(UUID userId, UUID clientId) {
        var clientLevelAvailableRolesUri =
                String.format("%s/admin/realms/default/users/%s/role-mappings/clients/%s/available",
                        keycloakServerAuthUrl, userId, clientId);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        return WebClient.create(clientLevelAvailableRolesUri).get().header(AUTHORIZATION, accessToken).retrieve().bodyToMono(String.class)
                .block();
    }

    public void updateUserRoles(UUID userId, UUID clientId, List<Map<String, Object>> roles) {
        var userClientRolesMappingUri =
                String.format("%s/admin/realms/default/users/%s/role-mappings/clients/%s", keycloakServerAuthUrl, userId, clientId);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        WebClient.create(userClientRolesMappingUri).post().header(AUTHORIZATION, accessToken).contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON).bodyValue(roles)
                .exchangeToMono(res -> Mono.just(res.statusCode())).block();
    }

    public String getClientSecret(UUID clientId) {
        var clientSecretUri = String.format("%s/admin/realms/default/clients/%s/client-secret", keycloakServerAuthUrl, clientId);
        var accessToken = BEARER + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

        return WebClient.create(clientSecretUri).get().header(AUTHORIZATION, accessToken).retrieve().bodyToMono(String.class)
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
            createUser(Map.of("username", username,
                    "email", email,
                    "enabled", enabled,
                    "attributes", new UserAttribute(organizationId, roles, displayName),
                    "credentials", List.of(Map.of("type", "password",
                            VALUE_KEY, password,
                            "temporary", passwordTemporary))));

            var userId = UUID.fromString(new JSONArray(getUsers(Map.of("username", username)))
                    .getJSONObject(0).getString("id"));
            userDetails.put("userId", userId);

            var clientId = UUID.fromString(new JSONArray(getClientDetails(Map.of("clientId", keycloakDefaultRealmDefaultClientId)))
                    .getJSONObject(0).getString("id"));
            userDetails.put("clientId", clientId);

            var clientSecret = getClientSecret(clientId);
            if (clientSecret.contains(VALUE_KEY)) {
                userDetails.put("clientSecret", new JSONObject(clientSecret).getString(VALUE_KEY));
            }

            var clientLevelMappingArray = new JSONArray(getClientLevelRoleMappings(userId, clientId));
            if (clientLevelMappingArray.length() > 0) {
                var clientLevelMappingDetails = clientLevelMappingArray.getJSONObject(0);
                updateUserRoles(userId, clientId, List.of(Map.of(
                        "id", clientLevelMappingDetails.getString("id"),
                        "name", clientLevelMappingDetails.getString("name"),
                        "description", clientLevelMappingDetails.getString("description"),
                        "composite", clientLevelMappingDetails.getBoolean("composite"),
                        "clientRole", clientLevelMappingDetails.getBoolean("clientRole"),
                        "containerId", clientLevelMappingDetails.getString("containerId"))));
            } else {
                LOGGER.warn("Roles are already mapped or no role mappings found.");
            }
        } catch (Exception e) {
            throw (KeycloakSynchronizationException)new KeycloakSynchronizationException(e.getMessage()).initCause(e);
        }
        return userDetails;
    }
}
