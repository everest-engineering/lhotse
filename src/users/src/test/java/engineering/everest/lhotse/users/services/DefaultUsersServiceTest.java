package engineering.everest.lhotse.users.services;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.users.domain.commands.AddUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.CreateOrganizationUserCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.RemoveUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.Role.ADMIN;
import static engineering.everest.lhotse.common.domain.Role.ORG_ADMIN;
import static engineering.everest.lhotse.common.domain.Role.ORG_USER;
import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultUsersServiceTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID PROFILE_PHOTO_FILE_ID = randomUUID();
    private static final String NEW_USER_EMAIL = "new-user-email";
    private static final String NEW_USER_DISPLAY_NAME = "new-user-display-name";

    @Mock
    private HazelcastCommandGateway commandGateway;
    @Mock
    private KeycloakSynchronizationService keycloakSynchronizationService;

    private DefaultUsersService defaultUsersService;

    @BeforeEach
    void setUp() {
        defaultUsersService = new DefaultUsersService(commandGateway, keycloakSynchronizationService);
    }

    @Test
    void updateUserDetails_WillSendCommand() {
        defaultUsersService.updateUser(ADMIN_ID, USER_ID, "email-change",
            "display-name-change");

        verify(commandGateway).sendAndWait(new UpdateUserDetailsCommand(USER_ID, "email-change",
            "display-name-change", ADMIN_ID));
    }

    @Test
    void addUserRoles_WillSendCommand() {
        var roles = Set.of(ORG_ADMIN, ORG_USER);
        defaultUsersService.addUserRoles(ADMIN_ID, USER_ID, roles);
        verify(commandGateway).sendAndWait(new AddUserRolesCommand(USER_ID, roles, ADMIN_ID));
    }

    @Test
    void removeUserRoles_WillSendCommand() {
        var roles = Set.of(ORG_ADMIN, ADMIN);
        defaultUsersService.removeUserRoles(ADMIN_ID, USER_ID, roles);
        verify(commandGateway).sendAndWait(new RemoveUserRolesCommand(USER_ID, roles, ADMIN_ID));
    }

    @Test
    void createNewKeycloakUser_WillSendCommandAndWaitForCompletion() {
        when(keycloakSynchronizationService.createNewKeycloakUserAndSendVerificationEmail(NEW_USER_EMAIL, ORGANIZATION_ID,
            NEW_USER_DISPLAY_NAME))
                .thenReturn(USER_ID);

        defaultUsersService.createOrganizationUser(ADMIN_ID, ORGANIZATION_ID, NEW_USER_EMAIL, NEW_USER_DISPLAY_NAME);
        verify(commandGateway)
            .sendAndWait(new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, NEW_USER_EMAIL, NEW_USER_DISPLAY_NAME));
    }

    @Test
    void storeProfilePhoto_WillSendCommand() {
        defaultUsersService.storeProfilePhoto(USER_ID, PROFILE_PHOTO_FILE_ID);
        verify(commandGateway).sendAndWait(new RegisterUploadedUserProfilePhotoCommand(USER_ID, PROFILE_PHOTO_FILE_ID));
    }

    @Test
    void deleteAndForget_WillSendCommand() {
        defaultUsersService.deleteAndForget(ADMIN_ID, USER_ID, "User requested and we do the right thing");
        verify(commandGateway).sendAndWait(new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, "User requested and we do the right thing"));
    }
}
