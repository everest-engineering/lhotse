package engineering.everest.lhotse.functionaltests.scenarios;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.hazelcast.core.HazelcastInstance;
import engineering.everest.lhotse.AdminProvisionTask;
import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.RegisterOrganizationRequest;
import engineering.everest.lhotse.api.rest.security.EntityPermissionEvaluator;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.registrations.persistence.PendingRegistrationsRepository;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
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
    private PendingRegistrationsRepository pendingRegistrationsRepository;
    @Value("${application.setup.admin.username}")
    private String adminUserName;
    @Autowired
    private AdminProvisionTask adminProvisionTask;
    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private HazelcastInstance hazelcastInstance;

    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient = new ApiRestTestClient(webTestClient, adminProvisionTask);
    }

    @Test
    void commandValidatingMessageHandlerInterceptorWillBeRegistered() {
        applicationContext.getBean(CommandValidatingMessageHandlerInterceptor.class);
    }

    @Test
    void entityPermissionEvaluatorWillBeRegistered() {
        applicationContext.getBean(EntityPermissionEvaluator.class);
    }

    @Test
    void adminWillBeProvisioned() {
        usersRepository.findByUsernameIgnoreCase(adminUserName).orElseThrow();
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
    void organizationsAndUsersCanBeCreated() {
        apiRestTestClient.createAdminUserAndLogin();
        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("user@example.com", "password", "Captain Fancypants");

        var organizationId = apiRestTestClient.createRegisteredOrganization(newOrganizationRequest, CREATED);
        var userId = apiRestTestClient.createUser(organizationId, newUserRequest, CREATED);
        apiRestTestClient.getUser(userId, OK);
    }

    @Test
    void newUsersCanRegisterTheirOrganizationAndCreateNewUsersInTheirOrganization() {
        apiRestTestClient.logout();
        var registerOrganizationRequest = new RegisterOrganizationRequest("Alice's Art Artefactory", "123 Any Street", "Melbourne", "Victoria", "Australia", "3000", "http://alicesartartefactory.com", "Alice", "+61 422 123 456", "alice@example.com", "alicerocks");
        var organizationRegistrationResponse =  apiRestTestClient.registerNewOrganization(registerOrganizationRequest, CREATED);
        var newOrganizationId = organizationRegistrationResponse.getNewOrganizationId();
        var pendingRegistration = pendingRegistrationsRepository.findByOrganizationId(newOrganizationId); // Confirmation code is "emailed", but we can cheat and pull it from the DB.

        apiRestTestClient.confirmOrganizationRegistration(newOrganizationId, pendingRegistration.getConfirmationCode(), OK);
        apiRestTestClient.login("alice@example.com", "alicerocks");
        var newUserRequest = new NewUserRequest("bob@example.com", "bobalsorocks", "My name is Bob");
        apiRestTestClient.createUser(newOrganizationId, newUserRequest, CREATED);
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class ErrorResponse {
        private HttpStatus status;
        private String message;
        private Map<String, String> timeStamp;
    }

    @Test
    void jsr303errorMessagesAreInternationalized() throws JsonProcessingException {
        apiRestTestClient.createAdminUserAndLogin();

        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("a-user", "password", "");
        var organizationId = apiRestTestClient.createRegisteredOrganization(newOrganizationRequest, CREATED);

        var response = webTestClient.post().uri("/api/organizations/{organizationId}/users", organizationId)
                .header("Authorization", "Bearer " + apiRestTestClient.getAccessToken())
                .header("Accept-Language", "de-DE")
                .contentType(APPLICATION_JSON)
                .body(fromValue(newUserRequest))
                .exchange()
                .returnResult(ErrorResponse.class)
                .getResponseBody()
                .blockFirst();
        assertEquals("displayName: darf nicht leer sein", response.getMessage());
    }

    @Test
    void domainValidationErrorMessagesAreInternationalized() throws JsonProcessingException {
        apiRestTestClient.createAdminUserAndLogin();

        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, "admin@example.com");
        var newUserRequest = new NewUserRequest("user123@example.com", "password", "Captain Fancypants");
        var organizationId = apiRestTestClient.createRegisteredOrganization(newOrganizationRequest, CREATED);
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
        assertEquals("Diese E-Mail Adresse ist bereits vergeben", response.getMessage());
    }
}
