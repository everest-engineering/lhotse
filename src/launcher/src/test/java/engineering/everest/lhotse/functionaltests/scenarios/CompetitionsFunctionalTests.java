package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;
import java.time.Instant;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
public class CompetitionsFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
    }

    @Test
    void adminUsersCanCreateCompetitions() {
        apiRestTestClient.createNewAdminKeycloakUserAndLogin("Admin001", "admin001@example.com");

        var competitionId = apiRestTestClient.createCompetition("Best holiday snaps", Instant.now(),
            Instant.now().plus(Duration.ofHours(2)), Instant.now().plus(Duration.ofHours(4)), CREATED);
        assertNotNull(competitionId);
    }
}
