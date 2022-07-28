package engineering.everest.lhotse.api.rest.controllers;

import engineering.everest.lhotse.api.rest.annotations.AdminOnly;
import engineering.everest.lhotse.api.rest.requests.DeleteAndForgetUserRequest;
import engineering.everest.lhotse.users.services.UsersService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

import javax.validation.Valid;
import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@Api(tags = "Users")
public class UsersController {

    private final UsersService usersService;

    @Autowired
    public UsersController(UsersService usersService) {
        this.usersService = usersService;
    }

    @PostMapping("/{userId}/forget")
    @ApiOperation("Handle a GDPR request to delete an account and scrub personal information")
    @AdminOnly
    public void forgetUser(@ApiIgnore Principal principal,
                           @PathVariable UUID userId,
                           @RequestBody @Valid DeleteAndForgetUserRequest request) {
        usersService.deleteAndForgetUser(UUID.fromString(principal.getName()), userId, request.getRequestReason());
    }
}
