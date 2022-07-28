package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.competitions.services.CompetitionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.UUID;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/api/competitions")
@Api(tags = "Competitions")
public class CompetitionsController {

    private final CompetitionsService competitionsService;

    public CompetitionsController(CompetitionsService competitionsService) {
        this.competitionsService = competitionsService;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @ApiOperation("Create a new competition to run")
    @AdminOnly
    public UUID createCompetition(@ApiIgnore Principal principal, @RequestBody CreateCompetitionRequest request) {
        return competitionsService.createCompetition(UUID.fromString(principal.getName()), request.getDescription(),
            request.getSubmissionsOpenTimestamp(), request.getSubmissionsCloseTimestamp(), request.getVotingEndsTimestamp(),
            request.getMaxEntriesPerUser());
    }
}
