package engineering.everest.starterkit.users.authserver;

import engineering.everest.starterkit.users.persistence.PersistableUser;
import engineering.everest.starterkit.users.persistence.UsersRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultAuthServerUserReadServiceTest {

    private DefaultAuthServerUserReadService defaultAuthServerUserReadService;

    @Mock
    private UsersRepository usersRepository;

    @BeforeEach
    void setUp() {
        defaultAuthServerUserReadService = new DefaultAuthServerUserReadService(usersRepository);
    }

    @Test
    void getByUsername_WillReturnAuthServerRepresentationOfPeristableUser() {
        PersistableUser persistableUser = mock(PersistableUser.class);
        when(persistableUser.getUsername()).thenReturn("found-username");
        when(persistableUser.getEncodedPassword()).thenReturn("encoded-password");
        when(persistableUser.isDisabled()).thenReturn(false);

        when(usersRepository.findByUsernameIgnoreCase("username")).thenReturn(Optional.of(persistableUser));

        AuthServerUser expectedAuthServerUser = new AuthServerUser("found-username", "encoded-password", false);
        assertEquals(expectedAuthServerUser, defaultAuthServerUserReadService.getByUsername("username"));
    }
}