package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.AdminProvisionTask;
import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.security.EntityPermissionEvaluator;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class ApplicationFunctionalTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UsersRepository usersRepository;
    @Value("${application.setup.admin.username}")
    private String adminUserName;
    @Autowired
    private AdminProvisionTask adminProvisionTask;
    @Autowired
    private WebTestClient webTestClient;

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
    void organizationsAndUsersCanBeCreated() {
        apiRestTestClient.createAdminUserAndLogin();
        var newOrganizationRequest = new NewOrganizationRequest("ACME", "123 King St", "Melbourne",
                "Vic", "Oz", "3000", null, null, null, null);
        var newUserRequest = new NewUserRequest("user@example.com", "password", "Captain Fancypants");

        var organizationId = apiRestTestClient.createOrganization(newOrganizationRequest, CREATED);
        var userId = apiRestTestClient.createUser(organizationId, newUserRequest, CREATED);
        apiRestTestClient.getUser(organizationId, userId, OK);
    }
}
