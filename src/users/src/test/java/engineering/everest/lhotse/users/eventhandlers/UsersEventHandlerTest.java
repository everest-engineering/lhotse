package engineering.everest.lhotse.users.eventhandlers;

import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import engineering.everest.starterkit.axon.cryptoshredding.CryptoShreddingKeyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.axon.common.domain.Role.ORG_ADMIN;
import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsersEventHandlerTest {

    private static final UUID USER_ID = randomUUID();
    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID PROFILE_PHOTO_FILE_ID = randomUUID();
    private static final Instant CREATION_TIME = Instant.ofEpochSecond(9999999L);
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String USER_USERNAME = "user-email";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String NO_CHANGE = null;
    private static final String BLANK_FIELD = "";

    private UsersEventHandler usersEventHandler;

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private CryptoShreddingKeyService cryptoShreddingKeyService;

    @BeforeEach
    void setUp() {
        usersEventHandler = new UsersEventHandler(usersRepository, cryptoShreddingKeyService);
    }

    @Test
    void prepareForReplay_willDeleteAllProjections() {
        usersEventHandler.prepareForReplay();

        verify(usersRepository).deleteByIdNot(ADMIN_ID);
    }

    @Test
    void onUserCreatedByAdminEvent_WillDelegate() {
        usersEventHandler.on(new UserCreatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID,
                USER_DISPLAY_NAME, USER_USERNAME, ENCODED_PASSWORD), CREATION_TIME);

        verify(usersRepository).createUser(USER_ID, ORGANIZATION_ID, USER_DISPLAY_NAME, USER_USERNAME, ENCODED_PASSWORD, CREATION_TIME);
    }

    @Test
    void onUserCreatedForNewlyRegisteredOrganizationEvent_WillDelegate() {
        var registrationConfirmationCode = randomUUID();
        usersEventHandler.on(new UserCreatedForNewlyRegisteredOrganizationEvent(USER_ID, ORGANIZATION_ID, registrationConfirmationCode,
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

    @Test
    void onUserPromotedToOrganizationAdminEvent_WillAddOrgAdminRoleToPromotedUser() {
        PersistableUser persistableUser = createPersistableUser();

        when(usersRepository.findById(USER_ID)).thenReturn(Optional.of(persistableUser));

        usersEventHandler.on(new UserPromotedToOrganizationAdminEvent(ORGANIZATION_ID, USER_ID));

        assertEquals(Set.of(ORG_ADMIN), persistableUser.getRoles());
        verify(usersRepository).save(persistableUser);
    }

    @Test
    void onUserDeletedAndForgottenEvent_WillDeleteUserAndDiscardSecretKey() {
        usersEventHandler.on(new UserDeletedAndForgottenEvent(USER_ID, ADMIN_ID, "It's the right thing to do"));

        verify(usersRepository).deleteById(USER_ID);
        verify(cryptoShreddingKeyService).deleteSecretKey(USER_ID.toString());
    }

    private static PersistableUser createPersistableUser() {
        PersistableUser persistableUser = new PersistableUser();
        persistableUser.setEmail("old-email");
        persistableUser.setDisplayName("old-display-name");
        persistableUser.setEncodedPassword("old-password");
        return persistableUser;
    }
}
