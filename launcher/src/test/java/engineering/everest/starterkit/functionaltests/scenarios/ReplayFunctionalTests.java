package engineering.everest.starterkit.functionaltests.scenarios;

import engineering.everest.starterkit.Launcher;
import engineering.everest.starterkit.functionaltests.helpers.ApiRestTestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(
        webEnvironment = RANDOM_PORT,
        classes = Launcher.class,
        properties = {"spring.data.mongodb.host=", "spring.data.mongodb.port="})
@ActiveProfiles("standalone")
class ReplayFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;

    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient = new ApiRestTestClient(webTestClient);
        apiRestTestClient.loginAsAdmin();
    }

    @Test
    void canGetReplayStatus() {
        Map<String, Object> replayStatus = apiRestTestClient.getReplayStatus(HttpStatus.OK);
        assertSame(Boolean.FALSE, replayStatus.get("isReplaying"));
    }

    @Test
    void canTriggerReplayEvents() {
        apiRestTestClient.triggerReplay(HttpStatus.NO_CONTENT);
    }

}
