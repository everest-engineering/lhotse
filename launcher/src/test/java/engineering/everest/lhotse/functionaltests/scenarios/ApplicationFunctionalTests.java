package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.api.rest.security.EntityPermissionEvaluator;
import engineering.everest.lhotse.axon.CommandValidatingMessageHandlerInterceptor;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class ApplicationFunctionalTests {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private UsersRepository usersRepository;
    @Value("${application.setup.admin.username}")
    private String adminUserName;

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
}
