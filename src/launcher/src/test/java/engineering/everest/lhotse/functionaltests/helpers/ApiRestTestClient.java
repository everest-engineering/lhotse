package engineering.everest.lhotse.functionaltests.helpers;

import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.tasks.AdminUserProvisioningTask;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
@Service
public class ApiRestTestClient {
    @Value("${keycloak.auth-server-url}")
    private String keycloakServerAuthUrl;
    @Value("${kc.server.admin-user}")
    private String keycloakAdminUser;
    @Value("${kc.server.admin-password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.realm}")
    private String keycloakAdminRealm;
    @Value("${keycloak.resource}")
    private String keycloakAdminClientId;
    @Value("${kc.server.connection.pool-size}")
    private int keycloakServerConnectionPoolSize;

    @Autowired
    private KeycloakSynchronizationService keycloakSynchronizationService;

    private final WebTestClient webTestClient;
    private final AdminUserProvisioningTask adminProvisionTask;
    private String accessToken;
    private String adminPassword;

    public ApiRestTestClient(WebTestClient webTestClient, AdminUserProvisioningTask adminProvisionTask) {
        this.webTestClient = webTestClient;
        this.adminProvisionTask = adminProvisionTask;
    }

    public void createAdminUserAndLogin() {
        var userDetails = adminProvisionTask.run();
        assertNotNull(userDetails);

        adminPassword = userDetails.getOrDefault("clientSecret", null).toString();
        assertNotNull(adminPassword);

        login(keycloakAdminUser, keycloakAdminPassword);
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void login(String username, String password) {
        Keycloak keycloak = KeycloakBuilder.builder()
            .serverUrl(keycloakServerAuthUrl)
            .grantType(OAuth2Constants.PASSWORD)
            .realm(keycloakAdminRealm)
            .clientId(keycloakAdminClientId)
            .clientSecret(adminPassword)
            .username(username)
            .password(password)
            .resteasyClient(new ResteasyClientBuilder()
                .connectionPoolSize(keycloakServerConnectionPoolSize).build())
            .build();

        assertNotNull(keycloak);
        var accessToken = keycloak.tokenManager().getAccessToken().getToken();
        assertNotNull(accessToken);
        this.accessToken = accessToken;
    }

    public void logout() {
        this.accessToken = null;
    }

    public UserResponse getUser(UUID userId, HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/api/users/{userId}", userId)
            .header("Authorization", "Bearer " + accessToken)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(UserResponse.class).getResponseBody().blockFirst();
    }

    public List<UserResponse> getAllUsers(HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/api/users")
            .header("Authorization", "Bearer " + accessToken)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(UserResponse.class).getResponseBody().buffer().blockFirst();
    }

    public List<OrganizationResponse> getAllOrganizations(HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/admin/organizations")
            .header("Authorization", "Bearer " + accessToken)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(OrganizationResponse.class).getResponseBody().buffer().blockFirst();
    }

    public UUID createUser(UUID organizationId, NewUserRequest request, HttpStatus expectedHttpStatus) {
        var responseSpec = webTestClient.post().uri("/api/organizations/{organizationId}/users", organizationId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .body(fromValue(request))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus);
        if (expectedHttpStatus == CREATED) {
            return responseSpec.returnResult(UUID.class).getResponseBody().blockFirst();
        }
        return null;
    }

    public void updateUser(UUID userId, UpdateUserRequest request, HttpStatus expectedHttpStatus) {
        webTestClient.put().uri("/api/users/{userId}", userId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .body(fromValue(request))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(UUID.class).getResponseBody().blockFirst();
    }

    public Map<String, Object> getReplayStatus(HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/actuator/replay")
            .header("Authorization", "Bearer " + accessToken)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(new ParameterizedTypeReference<Map<String, Object>>() {}).getResponseBody().blockFirst();
    }

    public void triggerReplay(HttpStatus expectedHttpStatus) {
        webTestClient.post().uri("/actuator/replay")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus);
    }
}
