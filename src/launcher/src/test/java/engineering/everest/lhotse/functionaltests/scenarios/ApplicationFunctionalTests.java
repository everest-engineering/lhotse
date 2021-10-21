package engineering.everest.lhotse.functionaltests.scenarios;

import com.hazelcast.core.HazelcastInstance;
import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.RegisterOrganizationRequest;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.BodyInserters.fromValue;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class ApplicationFunctionalTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private HazelcastInstance hazelcastInstance;
    @Autowired
    private ApiRestTestClient apiRestTestClient;

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

    @Test
    @Disabled("To be revisited. DB query is returning empty results.")
    void organizationsAndUsersCanBeCreated() {
        apiRestTestClient.createAdminUserAndLogin();
        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("user@example.com", "Captain Fancypants");

        var organizationId = apiRestTestClient.createOrganization(newOrganizationRequest, CREATED);
        var userId = apiRestTestClient.createUser(organizationId, newUserRequest, CREATED);
        apiRestTestClient.getUser(userId, OK);
    }

    @Test
    @Disabled("To be revisited. " +
            "Organization can be registered but login throws {\"error\":\"invalid_grant\",\"error_description\":\"Account is not fully set up\"}. " +
            "Because a user is expected to update his password at initial login.")
    void newUsersCanRegisterTheirOrganizationAndCreateNewUsersInTheirOrganization() throws InterruptedException {
        apiRestTestClient.logout();
        var registerOrganizationRequest = new RegisterOrganizationRequest("Alice's Art Artefactory", "123 Any Street", "Melbourne", "Victoria", "Australia", "3000", "http://alicesartartefactory.com", "Alice", "+61 422 123 456", "alice@example.com");
        var organizationRegistrationResponse = apiRestTestClient.registerNewOrganization(registerOrganizationRequest, CREATED);
        var newOrganizationId = organizationRegistrationResponse.getNewOrganizationId();
        Thread.sleep(2000); // Default is now tracking event processor

        apiRestTestClient.login("alice@example.com", "alicerocks");
        var newUserRequest = new NewUserRequest("bob@example.com", "My name is Bob");
        apiRestTestClient.createUser(newOrganizationId, newUserRequest, CREATED);
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

        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("a-user", "");
        var organizationId = apiRestTestClient.createOrganization(newOrganizationRequest, CREATED);

        var response = webTestClient.post().uri("/api/organizations/{organizationId}/users", organizationId)
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

        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("user123@example.com", "Captain Fancypants");
        var organizationId = apiRestTestClient.createOrganization(newOrganizationRequest, CREATED);
        apiRestTestClient.createUser(organizationId, newUserRequest, CREATED);

        var response = webTestClient.post().uri("/api/organizations/{organizationId}/users", organizationId)
                .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
                .header("Accept-Language", "de-DE")
                .contentType(APPLICATION_JSON)
                .body(fromValue(newUserRequest))
                .exchange()
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();
        assertNotNull(response);
        assertEquals("Diese E-Mail Adresse ist bereits vergeben", response.getMessage());
    }
}
