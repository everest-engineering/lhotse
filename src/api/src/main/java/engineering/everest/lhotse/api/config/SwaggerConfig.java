package engineering.everest.lhotse.api.config;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static springfox.documentation.builders.PathSelectors.regex;

@Profile("!prod")
@Configuration
@ComponentScan({ "engineering.everest.lhotse.api" })
@PropertySource("swagger.properties")
@SwaggerDefinition(
    tags = {
        @Tag(name = "System", description = "Information on the system you are interacting with"),
        @Tag(name = "Organizations", description = "Organization information and management"),
        @Tag(name = "Users", description = "User information and management"),
        @Tag(name = "OrgAdmins", description = "Organization Admin information and management"),
    })
public class SwaggerConfig {

    private final String authServer;
    private final String clientId;
    private final String realm;

    public SwaggerConfig(@Value("${keycloak.auth-server-url}") String authServer,
                         @Value("${keycloak.resource}") String clientId,
                         @Value("${keycloak.realm}") String realm) {
        this.authServer = authServer;
        this.clientId = clientId;
        this.realm = realm;
    }

    @Bean
    public Docket apiDocumentation() {
        return new Docket(DocumentationType.SWAGGER_2)
            .apiInfo(internalApiInfo())
            .produces(Set.of(APPLICATION_JSON_VALUE))
            .consumes(Set.of(APPLICATION_JSON_VALUE))
            .select()
            .apis(RequestHandlerSelectors.any())
            .paths(pathsToDocument())
            .build()
            .securitySchemes(List.of(securityScheme()))
            .securityContexts(List.of(securityContext()));
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
            .realm(realm)
            .clientId(clientId)
            .scopeSeparator(" ")
            .useBasicAuthenticationWithAccessCodeGrant(true)
            .build();
    }

    private ApiInfo internalApiInfo() {
        return new ApiInfoBuilder()
            .title("REST API")
            .description("Our REST API. Subject to change without notice.")
            .termsOfServiceUrl("http://my.project.web")
            .version("0.0")
            .build();
    }

    private Predicate<String> pathsToDocument() {
        return regex("/api/.*")
            .or(regex("/admin/.*"))
            .or(regex("/oauth/.*"))
            .or(regex("/tokens/.*"));
    }

    private SecurityScheme securityScheme() {
        var grantType = new ResourceOwnerPasswordCredentialsGrant(authServer + "/realms/" + realm + "/protocol/openid-connect/token");

        return new OAuthBuilder().name("Spring OAuth2")
            .grantTypes(List.of(grantType))
            .scopes(Arrays.asList(scopes()))
            .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(List.of(new SecurityReference("Spring OAuth2", scopes())))
            .build();
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[] {
            new AuthorizationScope("read", "for read operations"),
            new AuthorizationScope("write", "for write operations") };
    }
}
