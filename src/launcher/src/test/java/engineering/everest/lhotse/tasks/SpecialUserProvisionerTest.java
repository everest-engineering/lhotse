package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.common.domain.User;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.Role.ORG_USER;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpecialUserProvisionerTest {

    private final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    private static final UUID MEMBER_USER_ID = randomUUID();
    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final User MEMBER = new User(MEMBER_USER_ID, ORGANIZATION_ID, "user@example.com", "display name");

    private SpecialUserProvisioner specialUserProvisioner;

    @Mock
    private UsersRepository usersRepository;
    @Mock
    private KeycloakSynchronizationService keycloakSynchronizationService;
    @Mock
    private OrganizationsRepository organizationsRepository;

    @BeforeEach
    void setUp() {
        specialUserProvisioner =
            new SpecialUserProvisioner(clock, usersRepository, organizationsRepository, keycloakSynchronizationService);

        when(keycloakSynchronizationService.setupKeycloakUser(MEMBER.getEmail(), MEMBER.getEmail(), true, ORGANIZATION_ID, Set.of(ORG_USER),
            MEMBER.getDisplayName(), "password", false))
                .thenReturn(Map.of("userId", MEMBER_USER_ID));
    }

    @Test
    void provision_WillCreateUserInKeycloak() {
        when(usersRepository.findByEmailIgnoreCase(MEMBER.getEmail())).thenReturn(Optional.empty());

        specialUserProvisioner.provision(MEMBER, "password", Set.of(ORG_USER));

        verify(keycloakSynchronizationService).setupKeycloakUser(MEMBER.getEmail(), MEMBER.getEmail(), true,
            ORGANIZATION_ID, Set.of(ORG_USER), "display name", "password", false);
    }

    @Test
    void provision_WillCreateUserInLocalRepository() {
        when(usersRepository.findByEmailIgnoreCase(MEMBER.getEmail())).thenReturn(Optional.empty());

        specialUserProvisioner.provision(MEMBER, "password", Set.of(ORG_USER));

        verify(usersRepository).save(
            new PersistableUser(MEMBER_USER_ID, ORGANIZATION_ID, MEMBER.getEmail(), MEMBER.getDisplayName(), false, Instant.now(clock)));
    }

    @Test
    void provision_WillCreateOrganizationInLocalRepository() {
        when(usersRepository.findByEmailIgnoreCase(MEMBER.getEmail())).thenReturn(Optional.empty());

        specialUserProvisioner.provision(MEMBER, "password", Set.of(ORG_USER));

        verify(usersRepository).save(
            new PersistableUser(MEMBER_USER_ID, ORGANIZATION_ID, MEMBER.getEmail(), MEMBER.getDisplayName(), false, Instant.now(clock)));
    }

    @Test
    void provision_WillSkipCreatingAdminUser_WhenAdminUserAlreadyExists() {
        when(usersRepository.findByEmailIgnoreCase(MEMBER.getEmail())).thenReturn(Optional.of(mock(PersistableUser.class)));

        specialUserProvisioner.provision(MEMBER, "password", Set.of(ORG_USER));

        verifyNoMoreInteractions(usersRepository);
    }
}
