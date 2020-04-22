package engineering.everest.lhotse.functionaltests.helpers;

import engineering.everest.lhotse.AdminProvisionTask;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.web.reactive.server.WebTestClient.ResponseSpec;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

public class ApiRestTestClient {
    private static final String AUTHENTICATION_ENDPOINT = "/oauth/token";

    private final WebTestClient webTestClient;
    private final AdminProvisionTask adminProvisionTask;
    private String accessToken;

    public ApiRestTestClient(WebTestClient webTestClient, AdminProvisionTask adminProvisionTask) {
        this.webTestClient = webTestClient;
        this.adminProvisionTask = adminProvisionTask;
    }

    public void createAdminUserAndLogin() {
        adminProvisionTask.run();
        login("admin@everest.engineering", "ac0n3x72");
    }

    public void login(String username, String password) {
        Map<String, String> results = webTestClient.post().uri(AUTHENTICATION_ENDPOINT)
                .contentType(APPLICATION_FORM_URLENCODED)
                .body(fromValue(String.format("grant_type=password&username=%s&password=%s&client_id=web-app-ui", username, password)))
                .exchange()
                .expectStatus().isEqualTo(OK)
                .returnResult(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .getResponseBody().blockFirst();
        assertNotNull(results);
        String accessToken = results.get("access_token");
        assertNotNull(accessToken);
        this.accessToken = accessToken;
    }

    public UserResponse getUser(UUID organizationId, UUID userId, HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/api/users/{userId}", userId)
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isEqualTo(expectedHttpStatus)
                .returnResult(UserResponse.class).getResponseBody().blockFirst();
    }

    public UUID createOrganization(NewOrganizationRequest request, HttpStatus expectedHttpStatus) {
        ResponseSpec responseSpec = webTestClient.post().uri("/api/organizations")
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

    public List<OrganizationResponse> getAllOrganizations(HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/api/organizations")
                .header("Authorization", "Bearer " + accessToken)
                .exchange()
                .expectStatus().isEqualTo(expectedHttpStatus)
                .returnResult(OrganizationResponse.class).getResponseBody().buffer().blockFirst();
    }

    public UUID createUser(UUID organizationId, NewUserRequest request, HttpStatus expectedHttpStatus) {
        ResponseSpec responseSpec = webTestClient.post().uri("/api/organizations/{organizationId}/users", organizationId)
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

    public void updateUser(UUID organizationId, UUID userId, UpdateUserRequest request, HttpStatus expectedHttpStatus) {
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
                .returnResult(new ParameterizedTypeReference<Map<String, Object>>() {
                }).getResponseBody().blockFirst();
    }

    public void triggerReplay(HttpStatus expectedHttpStatus) {
        webTestClient.post().uri("/actuator/replay")
                .header("Authorization", "Bearer " + accessToken)
                .contentType(APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(expectedHttpStatus);
    }
}
