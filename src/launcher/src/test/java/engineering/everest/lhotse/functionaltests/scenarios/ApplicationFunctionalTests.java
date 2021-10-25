package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.RegisterOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.axon.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.services.KeycloakSynchronizationService;
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

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;
import static org.springframework.http.HttpStatus.CREATED;
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

    @Test
    void organizationsAndUsersCanBeCreatedAndUserDetailsCanBeUpdated() throws Exception {
        apiRestTestClient.createAdminUserAndLogin();
        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");

        var newUserRequest = new NewUserRequest("user@example.com", "Captain Fancypants");
        var organizationId = apiRestTestClient.createOrganization(newOrganizationRequest, CREATED);

        var waiter = new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, Duration.ofMinutes(1),
                sleepDuration -> MILLISECONDS.sleep(sleepDuration.toMillis()));
        waiter.waitOrThrow(() -> organizationsReadService.exists(organizationId), "organization registration projection update");

        var userId = apiRestTestClient.createUser(organizationId, newUserRequest, CREATED);
        waiter.waitOrThrow(() -> usersReadService.exists(userId), "user registration projection update");
        apiRestTestClient.getUser(userId, OK);

        var userUpdateRequest = new UpdateUserRequest("Captain Jack Sparrow", "jack@example.com");
        apiRestTestClient.updateUser(userId, userUpdateRequest, OK);
        waiter.waitOrThrow(() -> usersReadService.getById(userId).getEmail()
                        .equals(userUpdateRequest.getEmail()) || usersReadService.getById(userId).getDisplayName().equals(userUpdateRequest.getDisplayName()),
                "=> user email or displayName projection update");
        assertEquals(apiRestTestClient.getUser(userId, OK).getDisplayName(), userUpdateRequest.getDisplayName());
        assertEquals(apiRestTestClient.getUser(userId, OK).getEmail(), userUpdateRequest.getEmail());
    }

    @Test
    void newUsersCanRegisterTheirOrganizationAndCreateNewUsersInTheirOrganization() throws Exception {
        apiRestTestClient.logout();
        var registerOrganizationRequest = new RegisterOrganizationRequest("Alice's Art Artefactory", "123 Any Street", "Melbourne", "Victoria", "Australia", "3000", "http://alicesartartefactory.com", "Alice", "+61 422 123 456", "alice@example.com");
        var organizationRegistrationResponse = apiRestTestClient.registerNewOrganization(registerOrganizationRequest, CREATED);
        var newOrganizationId = organizationRegistrationResponse.getNewOrganizationId();
        var waiter = new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, Duration.ofMinutes(1),
                sleepDuration -> MILLISECONDS.sleep(sleepDuration.toMillis()));
        waiter.waitOrThrow(() -> organizationsReadService.exists(newOrganizationId), "organization registration projection update");

        keycloakSynchronizationService.setupKeycloakUser("kitty@example.com", "kitty@example.com", true, UUID.randomUUID(), Set.of(Role.ORG_USER, Role.ORG_ADMIN),
                "Kitty", "meow@123", false);
        apiRestTestClient.login("kitty@example.com", "meow@123");

        var newUserRequest = new NewUserRequest("bob@example.com", "My name is Bob");
        var userId = apiRestTestClient.createUser(newOrganizationId, newUserRequest, CREATED);
        waiter.waitOrThrow(() -> usersReadService.exists(userId), "user registration projection update");

        assertEquals(apiRestTestClient.getUser(userId, OK).getUsername(), newUserRequest.getUsername());
        assertEquals(apiRestTestClient.getUser(userId, OK).getDisplayName(), newUserRequest.getDisplayName());
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
    void domainValidationErrorMessagesAreInternationalized() throws Exception {
        apiRestTestClient.createAdminUserAndLogin();

        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("user123@example.com", "Captain Fancypants");
        var organizationId = apiRestTestClient.createOrganization(newOrganizationRequest, CREATED);

        var waiter = new RetryWithExponentialBackoff(Duration.ofMillis(500), 2L, Duration.ofMinutes(1),
                sleepDuration -> MILLISECONDS.sleep(sleepDuration.toMillis()));
        waiter.waitOrThrow(() -> organizationsReadService.exists(organizationId), "organization registration projection update");

        var userId = apiRestTestClient.createUser(organizationId, newUserRequest, CREATED);
        waiter.waitOrThrow(() -> usersReadService.exists(userId), "user registration projection update");

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
