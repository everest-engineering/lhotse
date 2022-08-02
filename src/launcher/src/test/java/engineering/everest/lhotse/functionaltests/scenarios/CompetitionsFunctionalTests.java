package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.photos.persistence.PhotosRepository;
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
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CREATED;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
public class CompetitionsFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;
    @Autowired
    private PhotosRepository photosRepository;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
    }

    @Test
    void adminUsersCanCreateCompetitions() {
        apiRestTestClient.createAdminUserAndLogin("Admin001", "admin001@example.com");
        var competitionId = apiRestTestClient.createCompetition("Best holiday snaps", Instant.now(),
            Instant.now().plus(Duration.ofHours(2)), Instant.now().plus(Duration.ofHours(4)), CREATED);

        assertNotNull(competitionId);
    }

    @Test
    void usersCanEnterCompetitions() throws Exception {
        apiRestTestClient.createAdminUserAndLogin("Admin00", "admin002@example.com");
        var competitionId = apiRestTestClient.createCompetition("Best holiday snaps", Instant.now(),
            Instant.now().plus(Duration.ofHours(2)), Instant.now().plus(Duration.ofHours(4)), CREATED);

        apiRestTestClient.createUserAndLogin("Happy Harry", "harry@example.com");
        var photoId = apiRestTestClient.uploadPhoto("test_photo_2.png", CREATED);

        RetryWithExponentialBackoff.withMaxDuration(Duration.ofSeconds(10)).waitOrThrow(
            () -> photosRepository.existsById(photoId), "photo upload projection");

        apiRestTestClient.submitPhoto(competitionId, photoId, CREATED);
        apiRestTestClient.submitPhoto(competitionId, photoId, BAD_REQUEST); // Same photo can't be submitted twice
    }
}
