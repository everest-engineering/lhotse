package engineering.everest.lhotse.users.services;

import engineering.everest.lhotse.api.services.KeycloakClient;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.users.domain.commands.AddUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.CreateOrganizationUserCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.RemoveUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class DefaultUsersService implements UsersService {

    private final CommandGateway commandGateway;
    private final KeycloakClient keycloakClient;

    public DefaultUsersService(CommandGateway commandGateway, KeycloakClient keycloakClient) {
        this.commandGateway = commandGateway;
        this.keycloakClient = keycloakClient;
    }

    @Override
    public void updateUser(UUID requestingUserId, UUID userId, String emailAddressChange, String displayNameChange) {
        commandGateway.sendAndWait(new UpdateUserDetailsCommand(userId, emailAddressChange, displayNameChange, requestingUserId));
    }

    @Override
    public void addUserRoles(UUID requestingUserId, UUID userId, Set<Role> roles) {
        commandGateway.sendAndWait(new AddUserRolesCommand(userId, roles, requestingUserId));
    }

    @Override
    public void removeUserRoles(UUID requestingUserId, UUID userId, Set<Role> roles) {
        commandGateway.sendAndWait(new RemoveUserRolesCommand(userId, roles, requestingUserId));
    }

    @Override
    public UUID createOrganizationUser(UUID requestingUserId, UUID organizationId, String emailAddress, String displayName) {
        var keycloakUserId = createUserAndRetrieveKeycloakUserId(emailAddress, displayName);
        return commandGateway.sendAndWait(
            new CreateOrganizationUserCommand(keycloakUserId, organizationId, requestingUserId, emailAddress, displayName));
    }

    @Override
    public void storeProfilePhoto(UUID requestingUserId, UUID profilePhotoFileId) {
        commandGateway.sendAndWait(new RegisterUploadedUserProfilePhotoCommand(requestingUserId, profilePhotoFileId));
    }

    @Override
    public void deleteAndForget(UUID requestingUserId, UUID userId, String requestReason) {
        commandGateway.sendAndWait(new DeleteAndForgetUserCommand(userId, requestingUserId, requestReason));
    }

    private UUID createUserAndRetrieveKeycloakUserId(String emailAddress, String displayName) {
        return keycloakClient.createNewKeycloakUserAndSendVerificationEmail(emailAddress, displayName);
    }
}
