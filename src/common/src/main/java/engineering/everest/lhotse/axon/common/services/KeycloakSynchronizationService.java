package engineering.everest.lhotse.axon.common.services;

import java.util.Map;
import java.util.UUID;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class KeycloakSynchronizationService {
    private static final String BEARER = "Bearer ";
    private static final String AUTHORIZATION = "Authorization";

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerAuthUrl;
    @Value("${kc.server.admin-user}")
    private String keycloakAdminUser;
    @Value("${kc.server.admin-password}")
    private String keycloakAdminPassword;
    @Value("${kc.server.master-realm.default.client-id}")
    private String keycloakAdminClientId;
    @Value("${kc.server.connection.pool-size}")
    private int keycloakServerConnectionPoolSize;

    private Keycloak getAdminKeycloakClientInstance() {
        return KeycloakBuilder.builder().serverUrl(keycloakServerAuthUrl).grantType(OAuth2Constants.PASSWORD)
                .realm("master").clientId(keycloakAdminClientId).username(keycloakAdminUser)
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

        var filters = new StringBuilder("?");
        if (!queryFilters.isEmpty()) {
            for (var filter : queryFilters.entrySet()) {
                filters.append(filter.getKey());
                filters.append('=');
                filters.append(filter.getValue());
                filters.append('&');
            }
            usersUri += filters;
        }

        return WebClient.create(usersUri).get().header(AUTHORIZATION, accessToken).retrieve().bodyToMono(String.class)
                .block();
    }

}
