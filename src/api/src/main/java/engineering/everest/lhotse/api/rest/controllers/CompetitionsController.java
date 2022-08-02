package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.annotations.AdminOrRegisteredUser;
import engineering.everest.lhotse.api.rest.annotations.RegisteredUser;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.CompetitionSubmissionRequest;
import engineering.everest.lhotse.api.rest.requests.CreateCompetitionRequest;
import engineering.everest.lhotse.api.rest.responses.CompetitionResponse;
import engineering.everest.lhotse.competitions.services.CompetitionsReadService;
import engineering.everest.lhotse.competitions.services.CompetitionsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/competitions")
@Api(tags = "Competitions")
public class CompetitionsController {

    private final DtoConverter dtoConverter;
    private final CompetitionsService competitionsService;
    private final CompetitionsReadService competitionsReadService;

    public CompetitionsController(DtoConverter dtoConverter,
                                  CompetitionsService competitionsService,
                                  CompetitionsReadService competitionsReadService) {
        this.dtoConverter = dtoConverter;
        this.competitionsService = competitionsService;
        this.competitionsReadService = competitionsReadService;
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

    @GetMapping
    @ResponseStatus(OK)
    @ApiOperation("List all competitions")
    @AdminOrRegisteredUser
    public List<CompetitionResponse> getAllCompetitions(@ApiIgnore Principal principal) {
        return competitionsReadService.getAllCompetitionsOrderedByDescVotingEndsTimestamp().stream()
            .map(dtoConverter::convert)
            .collect(toList());
    }

    @PostMapping("/{competitionId}/submission")
    @ResponseStatus(CREATED)
    @ApiOperation("Submit a photo to the competition")
    @RegisteredUser
    public void enterPhoto(@ApiIgnore Principal principal,
                           @PathVariable UUID competitionId,
                           @RequestBody CompetitionSubmissionRequest request) {
        competitionsService.submitPhoto(UUID.fromString(principal.getName()), competitionId, request.getPhotoId(),
            request.getSubmissionNotes());
    }
}
