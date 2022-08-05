package engineering.everest.lhotse.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DocumentationConfig {

    private final BuildProperties buildProperties;

    public DocumentationConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @Bean
    public OpenAPI openApi() {
        var securitySchemeName = "bearerAuth";
        var securityScheme = new SecurityScheme()
            .name(securitySchemeName)
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT");

        return new OpenAPI()
            .info(new Info().title("Photo competition API")
                .description("A DDD+ES+CQRS implementation of a simple demo domain")
                .version(buildProperties.getVersion().replace("'", ""))
                .license(new License().name("Apache 2.0 licensed").url("https://www.apache.org/licenses/LICENSE-2.0")))
            .components(new Components().addSecuritySchemes(securitySchemeName, securityScheme));
    }
}
