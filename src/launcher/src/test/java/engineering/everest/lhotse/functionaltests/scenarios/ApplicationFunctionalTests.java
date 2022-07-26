package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.users.services.UsersReadService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Instant;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
class ApplicationFunctionalTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;
    @Autowired
    private UsersReadService usersReadService;
    @Autowired
    private KeycloakSynchronizationService keycloakSynchronizationService;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
    }

    @Test
    void commandValidatingMessageHandlerInterceptorWillBeRegistered() {
        applicationContext.getBean(CommandValidatingMessageHandlerInterceptor.class);
    }

    @Test
    @Disabled("until admin account refactoring complete")
    void metricsEndpointPublishesAxonMetrics() {
        apiRestTestClient.loginAsMonitoringClient();

        webTestClient.get().uri("/actuator/metrics/commandBus.successCounter")
            .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
            .exchange()
            .expectStatus().isEqualTo(OK);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ErrorResponse {
        private HttpStatus status;
        private String message;
        private Instant timestamp;
    }

    // @Test
    // void jsr303errorMessagesAreInternationalized() {
    // apiRestTestClient.createAdminUserAndLogin();
    //
    // var newUserRequest = new NewUserRequest("a-user", "");
    // var response = webTestClient.post().uri("/api/organizations/{organizationId}/users",
    // ORGANIZATION_ID)
    // .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
    // .header("Accept-Language", "de-DE")
    // .contentType(APPLICATION_JSON)
    // .body(fromValue(newUserRequest))
    // .exchange()
    // .returnResult(ErrorResponse.class)
    // .getResponseBody()
    // .blockFirst();
    // assertNotNull(response);
    // assertEquals("displayName: darf nicht leer sein", response.getMessage());
    // }
    //
    // @Test
    // @Disabled("until admin account refactoring complete")
    // void domainValidationErrorMessagesAreInternationalized() throws Exception {
    // apiRestTestClient.createAdminUserAndLogin();
    //
    // var newUserRequest = new NewUserRequest("user123@example.com", "Captain Fancypants");
    // var userId = apiRestTestClient.createUser(ORGANIZATION_ID, newUserRequest, CREATED);
    // RetryWithExponentialBackoff.oneMinuteWaiter()
    // .waitOrThrow(() -> usersReadService.exists(userId), "user registration projection update");
    //
    // var response = webTestClient.post().uri("/api/organizations/{organizationId}/users", ORGANIZATION_ID)
    // .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
    // .header("Accept-Language", "de-DE")
    // .contentType(APPLICATION_JSON)
    // .body(fromValue(newUserRequest))
    // .exchange()
    // .returnResult(ErrorResponse.class)
    // .getResponseBody()
    // .blockFirst();
    // assertNotNull(response);
    // assertEquals("Diese E-Mail Adresse ist bereits vergeben", response.getMessage());
    // }
}
