package engineering.everest.lhotse.api.config;

import engineering.everest.lhotse.axon.common.domain.User;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.util.AntPathMatcher;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.OAuth;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
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
    private final List<String> anonymousUserAntPaths;
    private final List<String> authenticatedAntPaths;
    private final AntPathMatcher antPathMatcher;

    @SuppressWarnings("PMD.UseVarargs")
    public SwaggerConfig(
            @Value("${application.security.endpoint.matchers.anonymous}") String[] anonymousUserAntPaths,
            @Value("${application.security.endpoint.matchers.authenticated}") String[] authenticatedAntPaths,
            @Value("${application.security.endpoint.matchers.admin}") String[] adminUserAntPaths) {
        this.anonymousUserAntPaths = asList(anonymousUserAntPaths);
        this.authenticatedAntPaths = concat(stream(adminUserAntPaths), stream(authenticatedAntPaths))
                .collect(toList());
        this.antPathMatcher = new AntPathMatcher();
    }

    @Bean
    public Docket apiDocumentation() {
        var securityContext = SecurityContext.builder()
                .securityReferences(List.of(createOAuthSecurityReference()))
                .operationSelector(securityContextOperationSelector())
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

    private Predicate<OperationContext> securityContextOperationSelector() {
        return operationContext -> {
            var mappingPattern = operationContext.requestMappingPattern();
            var isAnonymousPath = anonymousUserAntPaths.stream()
                    .anyMatch(x -> antPathMatcher.match(x, mappingPattern));
            var isAuthenticatedPath = authenticatedAntPaths.stream()
                    .anyMatch(x -> antPathMatcher.match(x, mappingPattern));
            return !isAnonymousPath && isAuthenticatedPath;
        };
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
