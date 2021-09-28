package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.axon.common.domain.User;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static springfox.documentation.builders.PathSelectors.regex;

@Profile("!prod")
@Configuration
@ComponentScan({"engineering.everest.lhotse.api"})
@PropertySource("swagger.properties")
@SwaggerDefinition(tags = {
        @Tag(name = "System", description = "Information on the system you are interacting with"),
        @Tag(name = "Organizations", description = "Organization information and management"),
        @Tag(name = "Users", description = "User information and management"),
        @Tag(name = "OrgAdmins", description = "Organization Admin information and management"),
})
public class SwaggerConfig {

    @Bean
    public Docket apiDocumentation() {
        var securityContext = SecurityContext.builder()
                .securityReferences(List.of(createOAuthSecurityReference()))
                .build();

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(internalApiInfo())
                .ignoredParameterTypes(User.class)
                .produces(Set.of(APPLICATION_JSON_VALUE))
                .consumes(Set.of(APPLICATION_JSON_VALUE))
                .securitySchemes(List.of(oauthPasswordGrantScheme()))
                .securityContexts(List.of(securityContext))
                .select()
                .paths(pathsToDocument())
                .build();
    }

    private SecurityReference createOAuthSecurityReference() {
        return new SecurityReference("oauth",
                new AuthorizationScope[]{
                        new AuthorizationScope("all", "")
                });
    }

    private ApiInfo internalApiInfo() {
        return new ApiInfoBuilder()
                .title("REST API")
                .description("Our REST API. Subject to change without notice.")
                .termsOfServiceUrl("http://my.project.web")
                .version("0.0")
                .build();
    }

    private OAuth oauthPasswordGrantScheme() {
        var scopes = new AuthorizationScope[]{
                new AuthorizationScope("all", "")
        };
        var grant = new ResourceOwnerPasswordCredentialsGrant("/oauth/token");
        return new OAuth("oauth", List.of(scopes), List.of(grant));
    }

    private Predicate<String> pathsToDocument() {
        return regex("/api/.*")
                .or(regex("/admin/.*"))
                .or(regex("/oauth/.*"))
                .or(regex("/tokens/.*"));
    }
}
