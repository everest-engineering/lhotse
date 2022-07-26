package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
class SecurityFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @WithAnonymousUser
    void applicationIsAbleToStart() {
        webTestClient.get().uri("/api/version")
            .exchange()
            .expectStatus().isOk()
            .expectBody(String.class);
    }

    @Test
    @EnabledIfSystemProperty(named = "org.gradle.project.buildDir", matches = ".+")
    @WithAnonymousUser
    void swaggerApiDocIsAccessible() throws IOException {
        var apiContent = webTestClient.get().uri("/api/doc")
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class).getResponseBody().blockFirst();

        assertNotNull(apiContent);

        Files.writeString(
            Paths.get(System.getProperty("org.gradle.project.buildDir"), "web-app-api.json"),
            apiContent);
    }

    @Test
    void retrievingOrganizationListWillRedirectToLogin_WhenUserIsNotAuthenticated() {
        webTestClient.get().uri("/admin/organizations")
            .exchange()
            .expectStatus().isFound();
    }
}
