package engineering.everest.lhotse.api.rest.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api")
@Api(consumes = APPLICATION_JSON_VALUE, tags = "System")
public class VersionController {

    private final BuildProperties buildProperties;

    @Autowired
    public VersionController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping("/version")
    @ApiOperation("Version of the service")
    public String getVersion() {
        return buildProperties.getVersion().replaceAll("'", "");
    }
}
