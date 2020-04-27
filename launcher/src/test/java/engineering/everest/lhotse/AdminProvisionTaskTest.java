package engineering.everest.lhotse;


import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static engineering.everest.lhotse.axon.common.domain.Role.ADMIN;
import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
import static java.util.UUID.fromString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminProvisionTaskTest {

    private static final String ADMIN_DISPLAY_NAME = "Admin";
    private static final String ADMIN_USERNAME = "admin-username";
    private static final String ADMIN_PASSWORD = "admin-raw-password";
    private static final String ENCODED_PASSWORD = "admin-encoded-password";

    private AdminProvisionTask adminProvisionTask;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        adminProvisionTask = new AdminProvisionTask(clock, usersRepository, passwordEncoder, ADMIN_USERNAME, ADMIN_PASSWORD);

        when(passwordEncoder.encode(ADMIN_PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(usersRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)).thenReturn(Optional.empty());
    }

    @Test
    void run_WillCreateAdminUser_WhenAdminUserNotPresentInUsersProjection() {
        adminProvisionTask.run();

        verify(usersRepository).save(
                new PersistableUser(ADMIN_ID, null, ADMIN_USERNAME, ENCODED_PASSWORD,
                ADMIN_DISPLAY_NAME, false, EnumSet.of(ADMIN), Instant.now(clock)));
    }

    @Test
    void run_WillSkipCreatingAdminUser_WhenAdminUserAlreadyExists() {
        when(usersRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)).thenReturn(Optional.of(
                new PersistableUser(ADMIN_ID, null, ADMIN_USERNAME, ENCODED_PASSWORD, ADMIN_DISPLAY_NAME,
                        false, EnumSet.of(ADMIN), Instant.now(clock))));

        adminProvisionTask.run();

        verify(usersRepository, never()).save(any(PersistableUser.class));
    }

    @Test
    void replayCompleted_WillCreateAdminUser_WhenAdminUserNotPresentInUsersProjection() {
        adminProvisionTask.replayCompleted();

        verify(usersRepository).save(
                new PersistableUser(ADMIN_ID, null, ADMIN_USERNAME, ENCODED_PASSWORD,
                        ADMIN_DISPLAY_NAME, false, EnumSet.of(ADMIN), Instant.now(clock)));
    }

    @Test
    void replayCompleted_WillSkipCreatingAdminUser_WhenAdminUserAlreadyExists() {
        when(usersRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)).thenReturn(Optional.of(
                new PersistableUser(ADMIN_ID, null, ADMIN_USERNAME, ENCODED_PASSWORD, ADMIN_DISPLAY_NAME,
                        false, EnumSet.of(ADMIN), Instant.now(clock))));

        adminProvisionTask.replayCompleted();

        verify(usersRepository, never()).save(any(PersistableUser.class));
    }
}
