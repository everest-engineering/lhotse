package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.AdminProvisionTask;
import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static java.lang.Boolean.FALSE;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@ActiveProfiles("standalone")
class ReplayFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private AdminProvisionTask adminProvisionTask;

    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient = new ApiRestTestClient(webTestClient, adminProvisionTask);
        apiRestTestClient.createAdminUserAndLogin();
    }

    @Test
    void canGetReplayStatus() {
        Map<String, Object> replayStatus = apiRestTestClient.getReplayStatus(OK);
        assertSame(FALSE, replayStatus.get("isReplaying"));
    }

    @Test
    void canTriggerReplayEvents() {
        apiRestTestClient.triggerReplay(NO_CONTENT);
    }
}
