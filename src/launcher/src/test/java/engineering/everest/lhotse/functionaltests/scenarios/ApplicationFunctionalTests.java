package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;

import java.time.Instant;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
@ActiveProfiles("functionaltests")
@DirtiesContext // Avoids logging noise. Can remove when test containers support shutting down after Spring shut down
class ApplicationFunctionalTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
    }

    @Test
    @WithAnonymousUser
    void applicationIsAbleToStart() {
        webTestClient.get().uri("/api/version")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class);
    }

    @Test
    void commandValidatingMessageHandlerInterceptorWillBeRegistered() {
        applicationContext.getBean(CommandValidatingMessageHandlerInterceptor.class);
    }

    @Test
    void metricsEndpointPublishesAxonMetrics() throws Exception {
        // Trigger commands + events so that metrics are published
        apiRestTestClient.createUserAndLogin("Zoltan", "zoltan@example.com");
        apiRestTestClient.uploadPhoto("test_photo_1.png", CREATED);

        apiRestTestClient.loginAsMonitoringClient();
        webTestClient.get().uri("/actuator/metrics/commandBus.successCounter")
            .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
            .exchange()
            .expectStatus().isEqualTo(OK);
    }

    @Test
    void jsr303errorMessagesAreInternationalized() throws JSONException {
        apiRestTestClient.createAdminUserAndLogin("Admin Adam", "adam@example.com");

        var requestBody = new CreateCompetitionRequest(null, Instant.now(),
            Instant.now().plus(5, MINUTES), Instant.now().plus(10, MINUTES), 1);
        var response = webTestClient.post().uri("/api/competitions")
            .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
            .header("Accept-Language", "de-DE")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .returnResult(ErrorResponse.class)
            .getResponseBody()
            .blockFirst();
        assertNotNull(response);
        assertEquals("description: darf nicht leer sein", response.getMessage());
    }

    @Test
    void domainValidationErrorMessagesAreInternationalized() throws JSONException {
        apiRestTestClient.createAdminUserAndLogin("Admin Alice", "alice@example.com");

        var requestBody = new CreateCompetitionRequest("description", Instant.now(),
            Instant.now().plus(5, SECONDS), Instant.now().plus(10, SECONDS), 1);

        var response = webTestClient.post().uri("/api/competitions")
            .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
            .header("Accept-Language", "de-DE")
            .contentType(APPLICATION_JSON)
            .body(BodyInserters.fromValue(requestBody))
            .exchange()
            .returnResult(ErrorResponse.class)
            .getResponseBody()
            .blockFirst();
        assertNotNull(response);
        assertEquals("Einreichungen muessen mindestens PT10S offen sein", response.getMessage());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ErrorResponse {
        private HttpStatus status;
        private String message;
        private Instant timestamp;
    }
}
