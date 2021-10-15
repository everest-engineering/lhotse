package engineering.everest.lhotse.users.services;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.axon.common.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.users.domain.commands.CreateUserCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
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
    private PasswordEncoder passwordEncoder;
    @Mock
    private KeycloakSynchronizationService keycloakSynchronizationService;

    private DefaultUsersService defaultUsersService;

    @BeforeEach
    void setUp() {
        defaultUsersService = new DefaultUsersService(commandGateway, passwordEncoder, keycloakSynchronizationService);
    }

    @Test
    void updateUserDetails_WillSendCommandAndWaitForCompletion() {
        when(passwordEncoder.encode("password-change")).thenReturn("encoded-password-change");

        defaultUsersService.updateUser(ADMIN_ID, USER_ID, "email-change",
                "display-name-change", "password-change");

        verify(commandGateway).sendAndWait(new UpdateUserDetailsCommand(USER_ID, "email-change",
                "display-name-change", "encoded-password-change", ADMIN_ID));
    }

    @Test
    void createNewUser_WillSendCommandAndWaitForCompletion() {
        when(passwordEncoder.encode("raw-password")).thenReturn("encoded-password");
        when(keycloakSynchronizationService.getUsers(Map.of("username", NEW_USER_EMAIL)))
                .thenReturn(new JSONArray().put(0, new JSONObject().put("id", USER_ID)).toString());

        defaultUsersService.createUser(ADMIN_ID, ORGANIZATION_ID, NEW_USER_EMAIL, NEW_USER_DISPLAY_NAME, "raw-password");

        verify(commandGateway).sendAndWait(new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, NEW_USER_EMAIL, "encoded-password", NEW_USER_DISPLAY_NAME));
    }

    @Test
    void storeProfilePhoto_WillSendCommandAndWaitForCompletion() {
        defaultUsersService.storeProfilePhoto(USER_ID, PROFILE_PHOTO_FILE_ID);
        verify(commandGateway).sendAndWait(new RegisterUploadedUserProfilePhotoCommand(USER_ID, PROFILE_PHOTO_FILE_ID));
    }

    @Test
    void deleteAndForget_WillSendCommandAndWaitForCompletion() {
        defaultUsersService.deleteAndForget(ADMIN_ID, USER_ID, "User requested and we do the right thing");
        verify(commandGateway).sendAndWait(new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, "User requested and we do the right thing"));
    }
}
