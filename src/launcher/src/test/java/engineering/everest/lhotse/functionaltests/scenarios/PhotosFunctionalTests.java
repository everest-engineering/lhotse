package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import engineering.everest.lhotse.photos.services.PhotosReadService;
import engineering.everest.starterkit.filestorage.FileService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static java.time.Duration.ofSeconds;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.data.domain.Pageable.unpaged;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
@ActiveProfiles("functionaltests")
class PhotosFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;
    @Autowired
    private PhotosReadService photosReadService;
    @Autowired
    private FileService fileService;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
        fileService.markAllEphemeralFilesForDeletion();
    }

    @Test
    void registeredUsersCanOnlySeeTheirOwnUploadPhotos() throws Exception {
        apiRestTestClient.createUserAndLogin("Alice", "alice@example.com");
        var firstPhotoId = apiRestTestClient.uploadPhoto("test_photo_1.png", CREATED);
        var secondPhotoId = apiRestTestClient.uploadPhoto("test_photo_2.png", CREATED);

        var photosVisibleToAlice = apiRestTestClient.getAllPhotosForCurrentUser(OK);
        assertEquals(secondPhotoId, photosVisibleToAlice.get(0).getId());   // Most recent first by default
        assertEquals(firstPhotoId, photosVisibleToAlice.get(1).getId());

        apiRestTestClient.createUserAndLogin("Not Alice", "not-alice@example.com");
        assertTrue(apiRestTestClient.getAllPhotosForCurrentUser(OK).isEmpty());
    }

    @Test
    void uploadedPhotosCanBeRetrieved() throws Exception {
        apiRestTestClient.createUserAndLogin("Bob", "bob@example.com");
        var photoId = apiRestTestClient.uploadPhoto("test_photo_1.png", CREATED);

        try (var resourceAsStream = this.getClass().getResourceAsStream("/test_photo_1.png")) {
            var expected = resourceAsStream.readAllBytes();
            assertArrayEquals(expected, apiRestTestClient.downloadPhoto(photoId, OK));
        }
    }

    @Test
    void deletingUserAlsoDeletesTheirPhotos() throws Exception {
        var craigUserId = apiRestTestClient.createUserAndLogin("Craig", "craig@example.com");
        apiRestTestClient.uploadPhoto("test_photo_1.png", CREATED);
        apiRestTestClient.uploadPhoto("test_photo_2.png", CREATED);

        apiRestTestClient.createAdminUserAndLogin("User Deleting Admin", "user-deleting-admin@example.com");
        apiRestTestClient.deleteAndForgetUser(craigUserId, "GDPR request", OK);

        RetryWithExponentialBackoff.withMaxDuration(ofSeconds(20)).waitOrThrow(
            () -> photosReadService.getAllPhotos(craigUserId, unpaged()).isEmpty(), "photo deletion projection");

        // User not automatically deleted in Keycloak so we can still authenticate (as of now). This test requires an admin API for
        // retrieving photos for specific user. TODO.
        apiRestTestClient.login("craig@example.com");
        assertTrue(apiRestTestClient.getAllPhotosForCurrentUser(OK).isEmpty());
    }
}
