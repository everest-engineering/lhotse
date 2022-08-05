package engineering.everest.lhotse.functionaltests.helpers;

import engineering.everest.lhotse.api.rest.requests.CompetitionSubmissionRequest;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.api.rest.requests.DeleteAndForgetUserRequest;
import engineering.everest.lhotse.api.rest.responses.PhotoResponse;
import engineering.everest.lhotse.api.services.KeycloakClient;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.photos.persistence.PhotosRepository;
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

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.keycloak.OAuth2Constants.CLIENT_CREDENTIALS;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;

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
    private KeycloakClient keycloakClient;
    @Autowired
    private PhotosRepository photosRepository;

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

    public void login(String emailAddress) {
        var keycloak = KeycloakBuilder.builder()
            .serverUrl(keycloakServerAuthUrl)
            .grantType(OAuth2Constants.PASSWORD)
            .realm(keycloakRealm)
            .clientId(keycloakClientId)
            .username(emailAddress)
            .password(PASSWORD)
            .resteasyClient(new ResteasyClientBuilder()
                .connectionPoolSize(keycloakServerConnectionPoolSize).build())
            .build();

        assertNotNull(keycloak);
        var accessToken = keycloak.tokenManager().getAccessToken().getToken();
        assertNotNull(accessToken);
        this.accessToken = accessToken;
    }

    public UUID createUserAndLogin(String displayName, String emailAddress) {
        var userId = keycloakClient.createNewKeycloakUser(displayName, emailAddress, PASSWORD);
        login(emailAddress);
        return userId;
    }

    public UUID createAdminUserAndLogin(String displayName, String emailAddress) {
        var userId = keycloakClient.createNewAdminKeycloakUser(displayName, emailAddress, PASSWORD);
        login(emailAddress);
        return userId;
    }

    public void deleteAndForgetUser(UUID userIdToDelete, String reason, HttpStatus expectedHttpStatus) {
        webTestClient.post().uri("/api/users/{userId}/forget", userIdToDelete)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(new DeleteAndForgetUserRequest(reason)))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus);
    }

    public String getAccessToken() {
        return accessToken;
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

    public UUID uploadPhoto(String photoFilename, HttpStatus expectedHttpStatus) throws Exception {
        var testPhotoResource = new ClassPathResource(photoFilename);
        var multipartBodyBuilder = new MultipartBodyBuilder();
        multipartBodyBuilder.part("file", testPhotoResource).contentType(MULTIPART_FORM_DATA);

        var photoId = webTestClient.post().uri("/api/photos")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData(multipartBodyBuilder.build()))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(UUID.class)
            .getResponseBody()
            .blockFirst();

        RetryWithExponentialBackoff.withMaxDuration(Duration.ofSeconds(10)).waitOrThrow(
            () -> photosRepository.existsById(photoId), "photo upload projection");

        return photoId;
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

    public UUID createCompetition(String description,
                                  Instant submissionsOpen,
                                  Instant submissionsClose,
                                  Instant votingEnds,
                                  HttpStatus expectedHttpStatus) {
        var requestBody = new CreateCompetitionRequest(description, submissionsOpen, submissionsClose, votingEnds, 1);

        return webTestClient.post().uri("/api/competitions")
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus)
            .returnResult(UUID.class)
            .getResponseBody()
            .blockFirst();
    }

    public void submitPhoto(UUID competitionId, UUID photoId, HttpStatus expectedHttpStatus) {
        var requestBody = new CompetitionSubmissionRequest(photoId, "submission notes go here");

        webTestClient.post().uri("/api/competitions/{competitionId}/photos", competitionId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus);
    }

    public void voteForPhoto(UUID competitionId, UUID photoId, HttpStatus expectedHttpStatus) {
        webTestClient.post().uri("/api/competitions/{competitionId}/photos/{photoId}/vote", competitionId, photoId)
            .header("Authorization", "Bearer " + accessToken)
            .contentType(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(expectedHttpStatus);
    }
}
