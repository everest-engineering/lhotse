package engineering.everest.lhotse.config;

import org.springframework.boot.actuate.autoconfigure.endpoint.web.CorsEndpointProperties;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.ExposableEndpoint;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.actuate.endpoint.web.EndpointMapping;
import org.springframework.boot.actuate.endpoint.web.EndpointMediaTypes;
import org.springframework.boot.actuate.endpoint.web.WebEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ControllerEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.annotation.ServletEndpointsSupplier;
import org.springframework.boot.actuate.endpoint.web.servlet.WebMvcEndpointHandlerMapping;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;

// https://github.com/springfox/springfox/issues/3462#issuecomment-1010721223

@Configuration
public class SpringfoxTemporaryWorkaroundConfig {
    @Bean
    public WebMvcEndpointHandlerMapping webEndpointServletHandlerMapping(WebEndpointsSupplier webEndpointsSupplier,
                                                                         ServletEndpointsSupplier servletEndpointsSupplier,
                                                                         ControllerEndpointsSupplier controllerEndpointsSupplier,
                                                                         EndpointMediaTypes endpointMediaTypes,
                                                                         CorsEndpointProperties corsProperties,
                                                                         WebEndpointProperties webEndpointProperties) {
        var allEndpoints = new ArrayList<ExposableEndpoint<?>>();
        allEndpoints.addAll(webEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(servletEndpointsSupplier.getEndpoints());
        allEndpoints.addAll(controllerEndpointsSupplier.getEndpoints());
        var endpointMapping = new EndpointMapping(webEndpointProperties.getBasePath());
        // boolean shouldRegisterLinksMapping = this.shouldRegisterLinksMapping(webEndpointProperties, environment,
        // webEndpointProperties.getBasePath());
        var linksResolver = new EndpointLinksResolver(allEndpoints, webEndpointProperties.getBasePath());
        return new WebMvcEndpointHandlerMapping(endpointMapping, webEndpointsSupplier.getEndpoints(), endpointMediaTypes,
            corsProperties.toCorsConfiguration(),
            linksResolver, true);
    }

    //
    // private boolean shouldRegisterLinksMapping(WebEndpointProperties webEndpointProperties, Environment environment, String basePath) {
    // return webEndpointProperties.getDiscovery().isEnabled() && (StringUtils.hasText(basePath) ||
    // ManagementPortType.get(environment).equals(ManagementPortType.DIFFERENT));
    // }
}
