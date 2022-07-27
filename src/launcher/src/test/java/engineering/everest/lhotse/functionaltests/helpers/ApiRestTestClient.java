package engineering.everest.lhotse.functionaltests.helpers;

import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import lombok.extern.slf4j.Slf4j;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@Slf4j
@Service
public class ApiRestTestClient {

    private static final String MONITORING_CLIENT_ID = "monitoring";
    private static final String MONITORING_CLIENT_SECRET = "ac0n3x72";
    private static final String PASSWORD = "pa$$w0rd";

    @Value("${keycloak.auth-server-url}")
    private String keycloakServerAuthUrl;
    @Value("${kc.server.admin-email}")
    private String keycloakAdminEmailAddress;
    @Value("${kc.server.admin-password}")
    private String keycloakAdminPassword;
    @Value("${keycloak.realm}")
    private String keycloakRealm;
    @Value("${keycloak.resource}")
    private String keycloakClientId;
    @Value("${kc.server.connection.pool-size}")
    private int keycloakServerConnectionPoolSize;

    @Autowired
    private KeycloakSynchronizationService keycloakSynchronizationService;

    private WebTestClient webTestClient;
    private String accessToken;

    public void setWebTestClient(WebTestClient webTestClient) {
        this.webTestClient = webTestClient;
    }

    public void loginAsMonitoringClient() {
        var keycloak = KeycloakBuilder.builder()
            .serverUrl(keycloakServerAuthUrl)
            .grantType(CLIENT_CREDENTIALS)
            .realm(keycloakRealm)
            .clientId(MONITORING_CLIENT_ID)
            .clientSecret(MONITORING_CLIENT_SECRET)
            .resteasyClient(new ResteasyClientBuilder()
                .connectionPoolSize(keycloakServerConnectionPoolSize).build())
            .build();

        assertNotNull(keycloak);
        var accessToken = keycloak.tokenManager().getAccessToken().getToken();
        assertNotNull(accessToken);
        this.accessToken = accessToken;
    }

    public void login(String emailAddress, String password) {
        var keycloak = KeycloakBuilder.builder()
            .serverUrl(keycloakServerAuthUrl)
            .grantType(OAuth2Constants.PASSWORD)
            .realm(keycloakRealm)
            .clientId(keycloakClientId)
            .username(emailAddress)
            .password(password)
            .resteasyClient(new ResteasyClientBuilder()
                .connectionPoolSize(keycloakServerConnectionPoolSize).build())
            .build();

        assertNotNull(keycloak);
        var accessToken = keycloak.tokenManager().getAccessToken().getToken();
        assertNotNull(accessToken);
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
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

    public UUID createUserAndLogin(String displayName, String emailAddress) {
        var userId = keycloakSynchronizationService.createNewKeycloakUser(displayName, emailAddress, PASSWORD);
        login(emailAddress, PASSWORD);
        return userId;
    }

    public UUID uploadPhoto(String photoFilename, HttpStatus expectedHttpStatus) {
        var testPhotoResource = new ClassPathResource(photoFilename);
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", testPhotoResource).contentType(MULTIPART_FORM_DATA);

        return webTestClient.post().uri("/api/photos")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(UUID.class)
            .getResponseBody()
            .blockFirst();
    }

    public List<PhotoResponse> getAllPhotosForCurrentUser(HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/api/photos")
            .header("Authorization", "Bearer " + accessToken)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .expectBodyList(PhotoResponse.class)
            .returnResult()
            .getResponseBody();
    }

    public byte[] downloadPhoto(UUID photoId, HttpStatus expectedHttpStatus) {
        return webTestClient.get().uri("/api/photos/{photoId}", photoId)
            .header("Authorization", "Bearer " + accessToken)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .expectBody(new ParameterizedTypeReference<byte[]>() {})
            .returnResult()
            .getResponseBody();
    }
}
