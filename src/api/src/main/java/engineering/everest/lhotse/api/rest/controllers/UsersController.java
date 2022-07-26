package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.annotations.AdminOrRegisteredUser;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.DeleteAndForgetUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseStatus;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("/api/users")
@Api(tags = "Users")
public class UsersController {

    private final DtoConverter dtoConverter;
    private final UsersService usersService;
    private final UsersReadService usersReadService;

    @Autowired
    public UsersController(DtoConverter dtoConverter, UsersService usersService, UsersReadService usersReadService) {
        this.dtoConverter = dtoConverter;
        this.usersService = usersService;
        this.usersReadService = usersReadService;
    }

    @GetMapping
    @ApiOperation("Retrieves entire user list for all organisations")
    @AdminOnly
    public List<UserResponse> getAllUsers(@ApiIgnore Principal principal) {
        return usersReadService.getUsers()
            .stream().map(dtoConverter::convert)
            .collect(toList());
    }

    @PostMapping("/{userId}/forget")
    @ApiOperation("Handle a GDPR request to delete an account and scrub personal information")
    @AdminOnly
    public void deleteUser(@ApiIgnore Principal principal,
                           @PathVariable UUID userId,
                           @RequestBody @Valid DeleteAndForgetUserRequest request) {
        usersService.deleteAndForget(UUID.fromString(principal.getName()), userId, request.getRequestReason());
    }

    @GetMapping("/{userId}")
    @ApiOperation("Retrieves user details")
    @AdminOrRegisteredUser
    public UserResponse getUser(@ApiIgnore Principal principal, @PathVariable UUID userId) {
        return dtoConverter.convert(usersReadService.getById(userId));
    }

    @PutMapping("/{userId}")
    @ApiOperation("Update an organization user's details")
    @AdminOrRegisteredUser
    public void updateUser(@ApiIgnore Principal principal,
                           @PathVariable UUID userId,
                           @RequestBody @Valid UpdateUserRequest request) {
        usersService.updateUser(UUID.fromString(principal.getName()), userId, request.getEmailAddress(),
            request.getDisplayName());
    }

    @PostMapping("/{userId}/roles")
    @ResponseStatus(OK)
    @ApiOperation("Add user roles")
    @AdminOnly
    public void addUserRoles(@ApiIgnore Principal principal,
                             @PathVariable UUID userId,
                             @RequestBody Set<Role> roles) {
        usersService.addUserRoles(UUID.fromString(principal.getName()), userId, roles);
    }

    @DeleteMapping("/{userId}/roles")
    @ResponseStatus(OK)
    @ApiOperation("Remove user roles")
    @AdminOnly
    public void removeUserRoles(@ApiIgnore Principal principal,
                                @PathVariable UUID userId,
                                @RequestBody Set<Role> roles) {
        usersService.removeUserRoles(UUID.fromString(principal.getName()), userId, roles);
    }
}
