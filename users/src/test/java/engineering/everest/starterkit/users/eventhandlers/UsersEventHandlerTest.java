package engineering.everest.starterkit.users.eventhandlers;

import engineering.everest.starterkit.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.starterkit.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.starterkit.users.persistence.PersistableUser;
import engineering.everest.starterkit.users.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersEventHandlerTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID PROFILE_PHOTO_FILE_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final Instant CREATION_TIME = Instant.ofEpochSecond(9999999L);
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String USER_USERNAME = "user-email";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String NO_CHANGE = null;
    private static final String BLANK_FIELD = "";

    private UsersEventHandler usersEventHandler;

    @Mock
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        usersEventHandler = new UsersEventHandler(usersRepository);
    }

    @Test
    void onUserCreatedByAdminEvent_WillDelegate() {
        usersEventHandler.on(new UserCreatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID,
                USER_DISPLAY_NAME, USER_USERNAME, ENCODED_PASSWORD), CREATION_TIME);

        verify(usersRepository).createUser(USER_ID, ORGANIZATION_ID, USER_DISPLAY_NAME, USER_USERNAME, ENCODED_PASSWORD, CREATION_TIME);
    }

    @Test
    void onUserUpdatedByAdminEvent_WillPersistChanges_WhenFieldsHaveChanged() {
        PersistableUser persistableUser = createPersistableUser();

        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(persistableUser));

        usersEventHandler.on(new UserDetailsUpdatedByAdminEvent(USER_ID, ORGANIZATION_ID, "display-name-change",
                "email-change", "password-change", ADMIN_ID));

        assertEquals("display-name-change", persistableUser.getDisplayName());
        assertEquals("email-change", persistableUser.getEmail());
        assertEquals("password-change", persistableUser.getEncodedPassword());

        verify(usersRepository).save(persistableUser);
    }

    @Test
    void onUserDetailsUpdatedByAdminEvent_WillIgnoreFieldsThatAreNotBeingChanged() {
        PersistableUser persistableUser = createPersistableUser();

        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(persistableUser));

        usersEventHandler.on(new UserDetailsUpdatedByAdminEvent(USER_ID, ORGANIZATION_ID, NO_CHANGE,
                NO_CHANGE, NO_CHANGE, ADMIN_ID));

        assertEquals("old-display-name", persistableUser.getDisplayName());
        assertEquals("old-email", persistableUser.getEmail());
        assertEquals("old-password", persistableUser.getEncodedPassword());

        verify(usersRepository).save(persistableUser);
    }

    @Test
    void onUserDetailsUpdatedByAdminEvent_WillUpdateFieldsBeingBlanked() {
        PersistableUser persistableUser = createPersistableUser();

        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(persistableUser));

        usersEventHandler.on(new UserDetailsUpdatedByAdminEvent(USER_ID, ORGANIZATION_ID, BLANK_FIELD,
                BLANK_FIELD, BLANK_FIELD, ADMIN_ID));

        assertEquals(BLANK_FIELD, persistableUser.getDisplayName());
        assertEquals(BLANK_FIELD, persistableUser.getEmail());
        assertEquals(BLANK_FIELD, persistableUser.getEncodedPassword());

        verify(usersRepository).save(persistableUser);
    }

    @Test
    void onUserProfilePhotoUploadedEvent_WillUpdateProfilePhotoFileId() {
        PersistableUser persistableUser = createPersistableUser();

        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(persistableUser));

        usersEventHandler.on(new UserProfilePhotoUploadedEvent(USER_ID, PROFILE_PHOTO_FILE_ID));

        assertEquals(PROFILE_PHOTO_FILE_ID, persistableUser.getProfilePhotoFileId());
        verify(usersRepository).save(persistableUser);
    }

    private static PersistableUser createPersistableUser() {
        PersistableUser persistableUser = new PersistableUser();
        persistableUser.setEmail("old-email");
        persistableUser.setDisplayName("old-display-name");
        persistableUser.setEncodedPassword("old-password");
        return persistableUser;
    }
}
