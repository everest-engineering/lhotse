package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import engineering.everest.lhotse.functionaltests.helpers.ApiRestTestClient;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
public class PhotosFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;
    @Autowired
    private ApiRestTestClient apiRestTestClient;

    @BeforeEach
    void setUp() {
        apiRestTestClient.setWebTestClient(webTestClient);
    }

    @Test
    void registeredUsersCanOnlySeeTheirOwnUploadPhotos() {
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
    void uploadedPhotosCanBeRetrieved() throws IOException {
        apiRestTestClient.createUserAndLogin("Bob", "bob@example.com");
        var photoId = apiRestTestClient.uploadPhoto("test_photo_1.png", CREATED);

        try (var resourceAsStream = this.getClass().getResourceAsStream("/test_photo_1.png")) {
            var expected = resourceAsStream.readAllBytes();
            assertArrayEquals(expected, apiRestTestClient.downloadPhoto(photoId, OK));
        }
    }
}
