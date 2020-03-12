package engineering.everest.lhotse.security.userdetails;

import engineering.everest.lhotse.users.authserver.AuthServerUser;
import engineering.everest.lhotse.users.authserver.AuthServerUserReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServerUserDetailsServiceTest {

    private static final String USER_NAME = "foo@example.com";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final AuthServerUser AUTH_SERVER_USER = new AuthServerUser(USER_NAME, ENCODED_PASSWORD, false);
    private static final UserDetails USER_DETAILS = User.builder().username(USER_NAME).password(ENCODED_PASSWORD).roles().build();

    @Mock
    private AuthServerUserReadService authServerUserReadService;

    private AuthServerUserDetailsService authServerUserDetailsService;

    @BeforeEach
    void setUp() {
        authServerUserDetailsService = new AuthServerUserDetailsService(authServerUserReadService);
    }

    @Test
    void willLoadUserByUsername() {
        when(authServerUserReadService.getByUsername(USER_NAME)).thenReturn(AUTH_SERVER_USER);
        UserDetails userDetails = authServerUserDetailsService.loadUserByUsername(USER_NAME);

        assertEquals(USER_DETAILS, userDetails);
    }

    @Test
    void willThrowUsernameNotFoundException_WhenUserIsNotFound() {
        doThrow(NoSuchElementException.class).when(authServerUserReadService).getByUsername(USER_NAME);
        assertThrows(UsernameNotFoundException.class, () -> authServerUserDetailsService.loadUserByUsername(USER_NAME));
    }
}
