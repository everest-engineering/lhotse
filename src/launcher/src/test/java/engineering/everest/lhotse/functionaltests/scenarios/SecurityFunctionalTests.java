package engineering.everest.lhotse.functionaltests.scenarios;

import engineering.everest.lhotse.Launcher;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = Launcher.class)
@AutoConfigureEmbeddedDatabase(type = POSTGRES)
@ActiveProfiles("functionaltests")
class SecurityFunctionalTests {

    @Autowired
    private WebTestClient webTestClient;

    @LocalServerPort
    int serverPort;

    @Test
    @EnabledIfSystemProperty(named = "org.gradle.project.buildDir", matches = ".+")
    @WithAnonymousUser
    void swaggerApiDocIsAccessible() throws IOException {
        var apiContent = webTestClient.get().uri("/v3/api-docs")
            .exchange()
            .expectStatus().isOk()
            .returnResult(String.class).getResponseBody().blockFirst();

        assertNotNull(apiContent);

        Files.writeString(
            Paths.get(System.getProperty("org.gradle.project.buildDir"), "web-app-api.json"),
            apiContent);
    }

    @ParameterizedTest
    @MethodSource("authenticatedGetEndpoints")
    void retrievingAuthenticatedGetEndpointsWillReturnUnauthorised_WhenUserIsNotAuthenticated(String endpoint) {
        webTestClient.get().uri(endpoint)
            .exchange()
            .expectStatus().isUnauthorized();
    }

    private static Stream<Arguments> authenticatedGetEndpoints() {
        return Stream.of(
            Arguments.of("/api/competitions"),
            Arguments.of("/api/competitions/some-id"),
            Arguments.of("/api/photos"),
            Arguments.of("/api/photos/some-id"));
    }
}
