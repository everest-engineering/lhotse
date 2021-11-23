package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.services.UsersReadService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@SpringBootTest(webEnvironment = DEFINED_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class ApplicationFunctionalTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;
    @Autowired
    private OrganizationsReadService organizationsReadService;
    @Autowired
    private UsersReadService usersReadService;
    @Autowired
    private KeycloakSynchronizationService keycloakSynchronizationService;

    @Test
    void commandValidatingMessageHandlerInterceptorWillBeRegistered() {
        applicationContext.getBean(CommandValidatingMessageHandlerInterceptor.class);
    }

    @Test
    void metricsEndpointPublishesAxonMetrics() {
        apiRestTestClient.createAdminUserAndLogin();

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
        private Map<String, String> timestamp;
    }

    @Test
    void jsr303errorMessagesAreInternationalized() {
        apiRestTestClient.createAdminUserAndLogin();

        var newUserRequest = new NewUserRequest("a-user", "");
        var response = webTestClient.post().uri("/api/organizations/{organizationId}/users",
                        UUID.randomUUID())
                .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
                .header("Accept-Language", "de-DE")
                .contentType(APPLICATION_JSON)
                .body(fromValue(newUserRequest))
                .exchange()
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();
        assertNotNull(response);
        assertEquals("displayName: darf nicht leer sein", response.getMessage());
    }

    @Test
    void domainValidationErrorMessagesAreInternationalized() {
        apiRestTestClient.createAdminUserAndLogin();

        var newUserRequest = new NewUserRequest("user123@example.com", "Captain Fancypants");
        var organizationId = UUID.randomUUID();
        var response = webTestClient.post().uri("/api/organizations/{organizationId}/users",
                        organizationId)
                .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
                .header("Accept-Language", "de-DE")
                .contentType(APPLICATION_JSON)
                .body(fromValue(newUserRequest))
                .exchange()
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();
        assertNotNull(response);
        assertEquals(String.format("Organisation %s existiert nicht", organizationId), response.getMessage());
    }
}
