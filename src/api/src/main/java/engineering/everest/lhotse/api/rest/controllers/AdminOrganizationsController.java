package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.NewOrganizationRequest;
import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.organizations.services.OrganizationsService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import springfox.documentation.annotations.ApiIgnore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/admin/organizations")
@Api(consumes = APPLICATION_JSON_VALUE, tags = "Admin: Organizations")
public class AdminOrganizationsController {

    private final DtoConverter dtoConverter;
    private final OrganizationsService organizationsService;
    private final OrganizationsReadService organizationsReadService;

    @Autowired
    public AdminOrganizationsController(DtoConverter dtoConverter, OrganizationsService organizationsService,
            OrganizationsReadService organizationsReadService) {
        this.dtoConverter = dtoConverter;
        this.organizationsService = organizationsService;
        this.organizationsReadService = organizationsReadService;
    }

    @GetMapping
    @ResponseStatus(OK)
    @ApiOperation("Retrieves details of all organizations")
    @AdminOnly
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationsReadService.getOrganizations().stream().map(dtoConverter::convert).collect(toList());
    }

    @PostMapping
    @ResponseStatus(CREATED)
    @ApiOperation("Register a new organization")
    @AdminOnly
    public UUID newOrganization(@ApiIgnore Principal principal, @RequestBody @Valid NewOrganizationRequest request) {
        return organizationsService.createOrganization(UUID.fromString(principal.getName()),
                request.getOrganizationName(), request.getStreet(), request.getCity(), request.getState(),
                request.getCountry(), request.getPostalCode(), request.getWebsiteUrl(), request.getContactName(),
                request.getContactPhoneNumber(), request.getContactEmail());
    }

    @DeleteMapping("/{organizationId}")
    @ResponseStatus(OK)
    @ApiOperation("Disable an organization")
    @AdminOnly
    public void disableOrganization(@ApiIgnore Principal principal, @PathVariable UUID organizationId) {
        organizationsService.disableOrganization(UUID.fromString(principal.getName()), organizationId);
    }

    @PostMapping("/{organizationId}")
    @ResponseStatus(OK)
    @ApiOperation("Enable an organization")
    @AdminOnly
    public void enableOrganization(@ApiIgnore Principal principal, @PathVariable UUID organizationId) {
        organizationsService.enableOrganization(UUID.fromString(principal.getName()), organizationId);
    }
}
