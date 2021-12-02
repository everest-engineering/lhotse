package engineering.everest.lhotse.users.services;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.users.domain.commands.AddUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.CreateOrganizationUserCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.RemoveUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class DefaultUsersService implements UsersService {

    private final HazelcastCommandGateway commandGateway;
    private final KeycloakSynchronizationService keycloakSynchronizationService;

    public DefaultUsersService(HazelcastCommandGateway commandGateway, KeycloakSynchronizationService keycloakSynchronizationService) {
        this.commandGateway = commandGateway;
        this.keycloakSynchronizationService = keycloakSynchronizationService;
    }

    @Override
    public void updateUser(UUID requestingUserId, UUID userId, String emailChange, String displayNameChange) {
        commandGateway.sendAndWait(new UpdateUserDetailsCommand(userId, emailChange, displayNameChange, requestingUserId));
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
    public UUID createOrganizationUser(UUID requestingUserId, UUID organizationId, String username, String displayName) {
        var keycloakUserId = createUserAndRetrieveKeycloakUserId(username, organizationId, displayName);
        return commandGateway.sendAndWait(
            new CreateOrganizationUserCommand(keycloakUserId, organizationId, requestingUserId, username, displayName));
    }

    @Override
    public void storeProfilePhoto(UUID requestingUserId, UUID profilePhotoFileId) {
        commandGateway.sendAndWait(new RegisterUploadedUserProfilePhotoCommand(requestingUserId, profilePhotoFileId));
    }

    @Override
    public void deleteAndForget(UUID requestingUserId, UUID userId, String requestReason) {
        commandGateway.sendAndWait(new DeleteAndForgetUserCommand(userId, requestingUserId, requestReason));
    }

    private UUID createUserAndRetrieveKeycloakUserId(String username, UUID organizationId, String displayName) {
        return keycloakSynchronizationService.createNewKeycloakUserAndSendVerificationEmail(username, organizationId, displayName);
    }
}
