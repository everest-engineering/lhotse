package engineering.everest.lhotse.axon.common;

import java.util.UUID;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.core.publisher.Mono;

@Component
public class RestClient {
        @Value("${keycloak.auth-server-url}")
        private String keycloakServerAuthUrl;
        @Value("${kc.server.admin-user}")
        private String keycloakAdminUser;
        @Value("${kc.server.admin-password}")
        private String keycloakAdminPassword;
        @Value("${kc.server.master-realm.default.client-id}")
        private String keycloakAdminClientId;

        private Keycloak getAdminKeycloakClientInstance() {
                return KeycloakBuilder.builder().serverUrl(keycloakServerAuthUrl).grantType(OAuth2Constants.PASSWORD)
                                .realm("master").clientId(keycloakAdminClientId).username(keycloakAdminUser)
                                .password(keycloakAdminPassword)
                                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(10).build()).build();
        }

        public void updateUserAttributes(UUID userId, String attributes) {
                try {
                        String accessToken = "Bearer "
                                        + getAdminKeycloakClientInstance().tokenManager().getAccessToken().getToken();

                        String usersUri = String.format("%s/admin/realms/default/users/%s", keycloakServerAuthUrl,
                                        userId);

                        WebClient.create(usersUri).put().header("Authorization", accessToken)
                                        .contentType(MediaType.APPLICATION_JSON).accept(MediaType.APPLICATION_JSON)
                                        .bodyValue(attributes).exchangeToMono(res -> Mono.just(res.statusCode()))
                                        .block();
                } catch (Exception e) {
                        throw new RuntimeException(e);
                }
        }

}
