package engineering.everest.starterkit.axon.security.userdetails;

import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.users.services.UsersReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.NoSuchElementException;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserDetailsServiceTest {

    private static final String USER_NAME = "admin@example.com";
    private static final User USER = new User(randomUUID(), randomUUID(), USER_NAME, "a user");

    @Mock
    private UsersReadService usersReadService;

    private AdminUserDetailsService adminUserDetailsService;

    @BeforeEach
    void setUp() {
        adminUserDetailsService = new AdminUserDetailsService(usersReadService);
    }

    @Test
    void willLoadUserByUsername() {
        when(usersReadService.getUserByUsername(USER_NAME)).thenReturn(USER);
        UserDetails userDetails = adminUserDetailsService.loadUserByUsername(USER_NAME);

        assertEquals(new AdminUserDetails(USER), userDetails);
    }

    @Test
    void willThrowUsernameNotFoundException_WhenUserIsNotFound() {
        doThrow(NoSuchElementException.class).when(usersReadService).getUserByUsername(USER_NAME);
        assertThrows(UsernameNotFoundException.class, () -> adminUserDetailsService.loadUserByUsername(USER_NAME));
    }

}
