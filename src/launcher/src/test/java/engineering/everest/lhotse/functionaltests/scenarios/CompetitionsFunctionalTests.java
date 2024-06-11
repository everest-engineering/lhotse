package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.starterkit.filestorage.FileService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("functionaltests")
@DirtiesContext // Avoids logging noise. Can remove when test containers support shutting down after Spring shut down
class CompetitionsFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;
    @Autowired
    private FileService fileService;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
        fileService.markAllEphemeralFilesForDeletion();
    }

    @Test
    void adminUsersCanCreateCompetitions() throws JSONException {
        apiRestTestClient.createAdminUserAndLogin("Admin001", "admin001@example.com");
        var competitionId = apiRestTestClient.createCompetition("Best holiday snaps", Instant.now(),
            Instant.now().plus(Duration.ofHours(2)), Instant.now().plus(Duration.ofHours(4)), CREATED);

        assertNotNull(competitionId);
    }

    @Test
    void usersCanEnterCompetitionsAndVoteOnEntries() throws Exception {
        apiRestTestClient.createAdminUserAndLogin("Admin00", "admin002@example.com");
        var votingStartTimestamp = Instant.now().plus(Duration.ofSeconds(11));
        var competitionId = apiRestTestClient.createCompetition("Best holiday snaps", Instant.now(),
            votingStartTimestamp, Instant.now().plus(Duration.ofSeconds(50)), CREATED);

        apiRestTestClient.createUserAndLogin("Happy Harry", "harry@example.com");
        var harryPhotoId = apiRestTestClient.uploadPhoto("test_photo_2.png", CREATED);

        apiRestTestClient.submitPhoto(competitionId, harryPhotoId, CREATED);
        apiRestTestClient.submitPhoto(competitionId, harryPhotoId, BAD_REQUEST); // Same photo can't be submitted twice

        apiRestTestClient.createUserAndLogin("Laughing Larry", "larry@example.com");
        var larryPhotoId = apiRestTestClient.uploadPhoto("test_photo_1.png", CREATED);

        apiRestTestClient.submitPhoto(competitionId, larryPhotoId, CREATED);

        RetryWithExponentialBackoff.withMaxDuration(Duration.ofSeconds(11)).waitOrThrow(
            () -> Instant.now().isAfter(votingStartTimestamp), "waiting for voting to begin");

        apiRestTestClient.createUserAndLogin("Boasting Barry", "barry@example.com");
        apiRestTestClient.voteForPhoto(competitionId, larryPhotoId, CREATED);
        apiRestTestClient.voteForPhoto(competitionId, larryPhotoId, BAD_REQUEST); // Can only vote once for a photo
        apiRestTestClient.voteForPhoto(competitionId, harryPhotoId, CREATED); // Can vote for multiple photos
    }
}
