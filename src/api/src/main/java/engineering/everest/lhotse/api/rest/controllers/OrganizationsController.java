package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOrAdminOfTargetOrganization;
import engineering.everest.lhotse.api.rest.annotations.AdminOrUserOfTargetOrganization;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.NewUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateOrganizationRequest;
import engineering.everest.lhotse.api.rest.responses.OrganizationResponse;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.organizations.Organization;
import engineering.everest.lhotse.organizations.domain.queries.OrganizationQuery;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.organizations.services.OrganizationsService;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.OK;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.MediaType.APPLICATION_NDJSON_VALUE;

@RestController
@RequestMapping("/api/organizations")
@Api(consumes = APPLICATION_JSON_VALUE, tags = "Organizations")
@Slf4j
public class OrganizationsController {

    private final DtoConverter dtoConverter;
    private final OrganizationsService organizationsService;
    private final OrganizationsReadService organizationsReadService;
    private final UsersService usersService;
    private final UsersReadService usersReadService;
    private final QueryGateway queryGateway;

    @Autowired
    public OrganizationsController(DtoConverter dtoConverter,
                                   OrganizationsService organizationsService,
                                   OrganizationsReadService organizationsReadService,
                                   UsersService usersService,
                                   UsersReadService usersReadService,
                                   QueryGateway queryGateway) {
        this.dtoConverter = dtoConverter;
        this.organizationsService = organizationsService;
        this.organizationsReadService = organizationsReadService;
        this.usersService = usersService;
        this.usersReadService = usersReadService;
        this.queryGateway = queryGateway;
    }

    @GetMapping("/{organizationId}")
    @ResponseStatus(OK)
    @ApiOperation("Get information for an organization")
    @AdminOrUserOfTargetOrganization
    public OrganizationResponse getOrganization(@ApiIgnore Principal principal, @PathVariable UUID organizationId) {
        return dtoConverter.convert(organizationsReadService.getById(organizationId));
    }

    @GetMapping(value = "/{organizationId}", produces = APPLICATION_NDJSON_VALUE)
    @ResponseStatus(OK)
    @ApiOperation("Stream the state of an organization")
    @AdminOrUserOfTargetOrganization
    public Flux<OrganizationResponse> getOrganizationUpdates(@ApiIgnore Principal principal, @PathVariable UUID organizationId) {
        var subscriptionQueryResult =
            queryGateway.subscriptionQuery(new OrganizationQuery(organizationId), Organization.class, Organization.class);
        return subscriptionQueryResult.updates()
            .mergeWith(subscriptionQueryResult.initialResult())
            .map(dtoConverter::convert);
    }

    @PutMapping("/{organizationId}")
    @ResponseStatus(OK)
    @ApiOperation("Update Organization")
    @AdminOrAdminOfTargetOrganization
    public void updateOrganization(@ApiIgnore Principal principal,
                                   @PathVariable UUID organizationId,
                                   @RequestBody @Valid UpdateOrganizationRequest request) {
        organizationsService.updateOrganization(UUID.fromString(principal.getName()), organizationId,
            request.getOrganizationName(), request.getStreet(), request.getCity(), request.getState(),
            request.getCountry(), request.getPostalCode(), request.getWebsiteUrl(), request.getContactName(),
            request.getPhoneNumber(), request.getEmailAddress());
    }

    @GetMapping("/{organizationId}/users")
    @ApiOperation("Retrieve a list of users for an organization")
    @AdminOrUserOfTargetOrganization
    public List<UserResponse> listOrganizationUsers(@ApiIgnore Principal principal, @PathVariable UUID organizationId) {
        return usersReadService.getUsersForOrganization(organizationId).stream()
            .map(dtoConverter::convert)
            .collect(toList());
    }

    @PostMapping("/{organizationId}/users")
    @ApiOperation("Create a new user for an organization")
    @ResponseStatus(CREATED)
    @AdminOrAdminOfTargetOrganization
    public UUID createUser(@ApiIgnore Principal principal,
                           @PathVariable UUID organizationId,
                           @RequestBody @Valid NewUserRequest request) {
        return usersService.createOrganizationUser(UUID.fromString(principal.getName()), organizationId, request.getEmailAddress(),
            request.getDisplayName());
    }
}
