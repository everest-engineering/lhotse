package engineering.everest.lhotse;

import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminProvisionTaskTest {
    private static final String ADMIN_DISPLAY_NAME = "Admin";
    private static final String ADMIN_USERNAME = "admin-username";
    private static final String ADMIN_PASSWORD = "admin-raw-password";

    private AdminProvisionTask adminProvisionTask;

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    @Mock
    private UsersRepository usersRepository;
    @Mock
    private KeycloakSynchronizationService keycloakSynchronizationService;

    @BeforeEach
    void setUp() {
        adminProvisionTask = new AdminProvisionTask(clock, usersRepository, keycloakSynchronizationService, ADMIN_USERNAME, ADMIN_PASSWORD);

        when(keycloakSynchronizationService.setupKeycloakUser(ADMIN_USERNAME, ADMIN_USERNAME, true, AdminProvisionTask.ORGANIZATION_ID,
                Set.of(Role.ORG_USER, Role.ORG_ADMIN), ADMIN_DISPLAY_NAME, ADMIN_PASSWORD, false)).thenReturn(
                        Map.of("userId", ADMIN_ID)
        );
        when(usersRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)).thenReturn(Optional.empty());
    }

    @Test
    void run_WillCreateAdminUser_WhenAdminUserNotPresentInUsersProjection() {
        adminProvisionTask.run();

        verify(usersRepository).save(
                new PersistableUser(ADMIN_ID, AdminProvisionTask.ORGANIZATION_ID, ADMIN_USERNAME,
                        ADMIN_DISPLAY_NAME, false, Instant.now(clock)));
    }

    @Test
    void run_WillSkipCreatingAdminUser_WhenAdminUserAlreadyExists() {
        when(usersRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)).thenReturn(Optional.of(
                new PersistableUser(ADMIN_ID, AdminProvisionTask.ORGANIZATION_ID, ADMIN_USERNAME, ADMIN_DISPLAY_NAME,
                        false, Instant.now(clock))
        ));
        adminProvisionTask.run();

        verify(usersRepository, never()).save(any(PersistableUser.class));
    }

    @Test
    void replayCompleted_WillCreateAdminUser_WhenAdminUserNotPresentInUsersProjection() {
        adminProvisionTask.replayCompleted();

        verify(usersRepository).save(
                new PersistableUser(ADMIN_ID, AdminProvisionTask.ORGANIZATION_ID, ADMIN_USERNAME, ADMIN_DISPLAY_NAME,
                        false, Instant.now(clock)));
    }

    @Test
    void replayCompleted_WillSkipCreatingAdminUser_WhenAdminUserAlreadyExists() {
        when(usersRepository.findByUsernameIgnoreCase(ADMIN_USERNAME)).thenReturn(Optional.of(
                new PersistableUser(ADMIN_ID, AdminProvisionTask.ORGANIZATION_ID, ADMIN_USERNAME, ADMIN_DISPLAY_NAME,
                        false, Instant.now(clock))));
        adminProvisionTask.replayCompleted();

        verify(usersRepository, never()).save(any(PersistableUser.class));
    }
}
