package engineering.everest.starterkit.api.rest.controllers;

import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.users.services.UsersReadService;
import engineering.everest.starterkit.users.services.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import engineering.everest.starterkit.api.rest.annotations.AdminOnly;
import engineering.everest.starterkit.api.rest.converters.DtoConverter;
import engineering.everest.starterkit.api.rest.requests.UpdateUserRequest;
import engineering.everest.starterkit.api.rest.responses.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import javax.validation.Valid;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/api/users")
@Api(consumes = APPLICATION_JSON_VALUE, tags = "Users")
public class UsersController {

    private final DtoConverter dtoConverter;
    private final UsersService usersService;
    private final UsersReadService usersReadService;

    @Autowired
    public UsersController(DtoConverter dtoConverter,
                           UsersService usersService,
                           UsersReadService usersReadService) {
        this.dtoConverter = dtoConverter;
        this.usersService = usersService;
        this.usersReadService = usersReadService;
    }

    @GetMapping
    @ApiOperation(produces = APPLICATION_JSON_VALUE, value = "Retrieves entire user list for all organisations")
    @AdminOnly
    public List<UserResponse> getAllUsers() {
        return usersReadService.getUsers().stream()
                .map(dtoConverter::convert)
                .collect(toList());
    }

    @GetMapping("/{userId}")
    @ApiOperation(produces = APPLICATION_JSON_VALUE, value = "Retrieves user details")
    @PostAuthorize("hasRole('ADMIN') or returnObject.organizationId == #requestingUser.organizationId")
    public UserResponse getUser(User requestingUser, @PathVariable UUID userId) {
        return dtoConverter.convert(usersReadService.getById(userId));
    }

    @PutMapping("/{userId}")
    @ApiOperation("Update an organization user's details")
    @PreAuthorize("#requestingUser.id == #userId or hasPermission(#userId, 'User', 'update')")
    public void updateUser(User requestingUser, @PathVariable UUID userId, @RequestBody @Valid UpdateUserRequest request) {
        usersService.updateUser(requestingUser.getId(), userId,
                request.getEmail(), request.getDisplayName(), request.getPassword());
    }
}
