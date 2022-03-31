package engineering.everest.lhotse.users.persistence;

import engineering.everest.lhotse.common.domain.User;
import engineering.everest.lhotse.users.config.TestUserSessionsJpaConfig;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.InputStreamOfKnownLength;
import engineering.everest.starterkit.media.thumbnails.ThumbnailService;
import io.zonky.test.db.AutoConfigureEmbeddedDatabase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.UUID;

import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.DatabaseType.POSTGRES;
import static io.zonky.test.db.AutoConfigureEmbeddedDatabase.RefreshMode.AFTER_EACH_TEST_METHOD;
import static java.time.Instant.ofEpochSecond;
import static java.util.Arrays.asList;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@AutoConfigureEmbeddedDatabase(refresh = AFTER_EACH_TEST_METHOD, type = POSTGRES)
@ExtendWith(SpringExtension.class)
@DataJpaTest
@EnableAutoConfiguration
@ComponentScan(basePackages = "engineering.everest.lhotse.users")
@ContextConfiguration(classes = { TestUserSessionsJpaConfig.class })
class UsersReadServiceIntegrationTest {

    private static final UUID ORG_1_USER_ID_1 = randomUUID();
    private static final UUID ORG_1_USER_ID_2 = randomUUID();
    private static final UUID ORG_1_USER_ID_3 = randomUUID();
    private static final UUID ORG_2_USER_ID_1 = randomUUID();
    private static final UUID ORGANIZATION_ID_1 = randomUUID();
    private static final UUID ORGANIZATION_ID_2 = randomUUID();
    private static final UUID PROFILE_PHOTO_ID = randomUUID();
    private static final String USER_DISPLAY_NAME_1 = "user-display-name-1";
    private static final String USER_DISPLAY_NAME_2 = "user-display-name-2";
    private static final String USER_DISPLAY_NAME_4 = "user-display-name-3";
    private static final String USER_DISPLAY_NAME_3 = "user-display-name-4";
    private static final String USER_EMAIL_ADDRESS_1 = "user1@email.com";
    private static final String USER_EMAIL_1_DIFFERENT_CASE = "USER1@email.com";
    private static final String USER_EMAIL_ADDRESS_2 = "user2@email.com";
    private static final String USER_EMAIL_ADDRESS_3 = "user3@email.com";
    private static final String USER_EMAIL_ADDRESS_4 = "user4@email.com";
    private static final String USER_EMAIL_DOES_NOT_EXIST = "unknown@email.com";
    private static final Instant CREATED_ON_1 = ofEpochSecond(800000L);
    private static final Instant CREATED_ON_2 = ofEpochSecond(800000L);
    private static final Instant CREATED_ON_4 = ofEpochSecond(800000L);
    private static final Instant CREATED_ON_3 = ofEpochSecond(800000L);

    private static final User ORG_1_USER_1 =
        new User(ORG_1_USER_ID_1, ORGANIZATION_ID_1, USER_DISPLAY_NAME_1, USER_EMAIL_ADDRESS_1, false);
    private static final User ORG_1_USER_2 = new User(ORG_1_USER_ID_2, ORGANIZATION_ID_1, USER_DISPLAY_NAME_2, USER_EMAIL_ADDRESS_2, false);
    private static final User ORG_1_USER_3_DISABLED =
        new User(ORG_1_USER_ID_3, ORGANIZATION_ID_1, USER_DISPLAY_NAME_3, USER_EMAIL_ADDRESS_3, true);
    private static final User ORG_2_USER_1 = new User(ORG_2_USER_ID_1, ORGANIZATION_ID_2, USER_DISPLAY_NAME_4, USER_EMAIL_ADDRESS_4, false);

    private static final PersistableUser PERSISTABLE_ORG_1_USER_1 =
        new PersistableUser(ORG_1_USER_ID_1, ORGANIZATION_ID_1, USER_DISPLAY_NAME_1, USER_EMAIL_ADDRESS_1, false, CREATED_ON_1,
            PROFILE_PHOTO_ID);
    private static final PersistableUser PERSISTABLE_ORG_1_USER_3 =
        new PersistableUser(ORG_1_USER_ID_3, ORGANIZATION_ID_1, USER_DISPLAY_NAME_3, USER_EMAIL_ADDRESS_3, true, CREATED_ON_3);
    public static final String PROFILE_PHOTO_FILE_CONTENTS = "my profile photo";

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private UsersReadService usersReadService;
    @MockBean
    private FileService fileService;
    @MockBean
    private ThumbnailService thumbnailService;

    @BeforeEach
    void setUp() {
        usersRepository.save(PERSISTABLE_ORG_1_USER_1);
        usersRepository.createUser(ORG_1_USER_ID_2, ORGANIZATION_ID_1, USER_DISPLAY_NAME_2, USER_EMAIL_ADDRESS_2, CREATED_ON_2);
        usersRepository.save(PERSISTABLE_ORG_1_USER_3);
        usersRepository.createUser(ORG_2_USER_ID_1, ORGANIZATION_ID_2, USER_DISPLAY_NAME_4, USER_EMAIL_ADDRESS_4, CREATED_ON_4);
    }

    @Test
    void canRetrieveSingleUser() {
        Assertions.assertEquals(ORG_2_USER_1, usersReadService.getById(ORG_2_USER_ID_1));
    }

    @Test
    void getUserByEmailAddress_WillRetrieveByEmailAddress() {
        assertEquals(ORG_2_USER_1, usersReadService.getUserByEmailAddress(USER_EMAIL_ADDRESS_4));
    }

    @Test
    void globalOrUserList_WillIncludeUsersFromAllOrganizations() {
        assertEquals(asList(ORG_1_USER_1, ORG_1_USER_2, ORG_1_USER_3_DISABLED, ORG_2_USER_1), usersReadService.getUsers());
    }

    @Test
    void userListForOrganization_WillFilterToOrganization() {
        assertEquals(asList(ORG_1_USER_1, ORG_1_USER_2, ORG_1_USER_3_DISABLED),
            usersReadService.getUsersForOrganization(ORGANIZATION_ID_1));
    }

    @Test
    void exist_WillReturnTrue_WhenUserExists() {
        assertTrue(usersReadService.exists(ORG_1_USER_ID_2));
    }

    @Test
    void exist_WillReturnFalse_WhenUserIsUnknown() {
        assertFalse(usersReadService.exists(randomUUID()));
    }

    @Test
    void hasUserWithEmail_WillReturnFalse_WhenUserWithEmailDoesNotExist() {
        assertFalse(usersReadService.hasUserWithEmail(USER_EMAIL_DOES_NOT_EXIST));
    }

    @Test
    void hasUserWithEmail_WillReturnTrue_WhenUserWithEmailExists() {
        assertTrue(usersReadService.hasUserWithEmail(USER_EMAIL_ADDRESS_1));
    }

    @Test
    void hasUserWithEmail_WillReturnTrue_WhenEmailIsDifferentlyCased() {
        assertTrue(usersReadService.hasUserWithEmail(USER_EMAIL_1_DIFFERENT_CASE));
    }

    @Test
    void getProfilePhotoStream_WillReturnStreamForProfilePhoto() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(PROFILE_PHOTO_FILE_CONTENTS.getBytes());
        when(fileService.stream(PROFILE_PHOTO_ID))
            .thenReturn(new InputStreamOfKnownLength(inputStream, PROFILE_PHOTO_FILE_CONTENTS.length()));

        assertEquals(inputStream, usersReadService.getProfilePhotoStream(ORG_1_USER_1.getId()));
    }

    @Test
    void getProfilePhotoStream_WillFail_WhenProfilePhotoNotUploaded() {
        assertThrows(NoSuchElementException.class, () -> usersReadService.getProfilePhotoStream(ORG_1_USER_ID_3));
    }

    @Test
    void getProfilePhotoThumbnailStream_WillReturnStreamForProfilePhoto() throws IOException {
        ByteArrayInputStream inputStream = new ByteArrayInputStream("my profile photo thumbnail".getBytes());
        when(thumbnailService.streamThumbnailForOriginalFile(PROFILE_PHOTO_ID, 100, 100)).thenReturn(inputStream);

        assertEquals(inputStream, usersReadService.getProfilePhotoThumbnailStream(ORG_1_USER_1.getId(), 100, 100));
    }

    @Test
    void getProfilePhotoThumbnailStream_WillFail_WhenProfilePhotoNotUploaded() {
        assertThrows(NoSuchElementException.class, () -> usersReadService.getProfilePhotoThumbnailStream(ORG_1_USER_ID_3, 100, 100));
    }
}
