package engineering.everest.lhotse.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;

import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.OAuthBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.GrantType;
import springfox.documentation.service.ResourceOwnerPasswordCredentialsGrant;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.service.SecurityScheme;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.SecurityConfiguration;
import springfox.documentation.swagger.web.SecurityConfigurationBuilder;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static springfox.documentation.builders.PathSelectors.regex;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

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
    @Value("${keycloak.auth-server-url}")
    private String authServer;
   
    @Value("${keycloak.credentials.secret}")
    private String clientSecret;
   
    @Value("${keycloak.resource}")
    private String cliendId;
   
    @Value("${keycloak.realm}")
    private String realm;

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
            .securitySchemes(Arrays.asList(securityScheme()))
            .securityContexts(Arrays.asList(securityContext()));
    }

    @Bean
    public SecurityConfiguration security() {
        return SecurityConfigurationBuilder.builder()
            .realm(realm)
            .clientId(cliendId)
            .clientSecret(clientSecret)
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
        GrantType grantType = 
        new ResourceOwnerPasswordCredentialsGrant(authServer + "/realms/" + realm + "/protocol/openid-connect/token");    
        
        return new OAuthBuilder().name("Spring OAuth2")
            .grantTypes(Arrays.asList(grantType))
            .scopes(Arrays.asList(scopes()))
            .build();
    }

    private SecurityContext securityContext() {
        return SecurityContext.builder()
            .securityReferences(Arrays.asList(new SecurityReference("Spring OAuth2", scopes())))
            .build();
    }

    private AuthorizationScope[] scopes() {
        return new AuthorizationScope[] { 
            new AuthorizationScope("read", "for read operations"), 
            new AuthorizationScope("write", "for write operations"), 
            new AuthorizationScope("foo", "Access foo API") };
    }
}
