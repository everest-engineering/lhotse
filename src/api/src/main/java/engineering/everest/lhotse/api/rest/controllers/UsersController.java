package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.converters.DtoConverter;
import engineering.everest.lhotse.api.rest.requests.DeleteAndForgetUserRequest;
import engineering.everest.lhotse.api.rest.requests.UpdateUserRequest;
import engineering.everest.lhotse.api.rest.responses.UserResponse;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.lhotse.users.services.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

import static java.util.stream.Collectors.toList;

@RestController
@RequestMapping("/api/users")
@Api(tags = "Users")
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
    @ApiOperation("Retrieves entire user list for all organisations")
    @AdminOnly

    public List<UserResponse> getAllUsers() {
        return usersReadService.getUsers().stream()
                .map(dtoConverter::convert)
                .collect(toList());
    }

    @PostMapping("/{userId}/forget")
    @ApiOperation("Handle a GDPR request to delete an account and scrub personal information")
    @AdminOnly
    public void deleteUser(User requestinguser,
                           @PathVariable UUID userId,
                           @RequestBody @Valid DeleteAndForgetUserRequest request) {
        usersService.deleteAndForget(requestinguser.getId(), userId, request.getRequestReason());
    }

    @GetMapping("/{userId}")
    @ApiOperation("Retrieves user details")
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
