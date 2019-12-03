package engineering.everest.starterkit.axon.security;

import engineering.everest.starterkit.axon.security.userdetails.AuthServerUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationProviderTest {

    private static final String USER_NAME = "foo@example.com";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final UserDetails USER_DETAILS = User.builder().username(USER_NAME).password(ENCODED_PASSWORD).roles().build();
    private static final UsernamePasswordAuthenticationToken USERNAME_PASSWORD_AUTHENTICATION_TOKEN =
            new UsernamePasswordAuthenticationToken(USER_DETAILS, ENCODED_PASSWORD);


    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthServerUserDetailsService authServerUserDetailsService;

    private AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        authenticationProvider = new AuthenticationProvider(passwordEncoder, authServerUserDetailsService);
    }

    @Test
    void willSupportAnyAuthenticationClass() {
        assertTrue(authenticationProvider.supports(Object.class));
    }

    @Test
    void retrieveUserWillDelegate() {
        authenticationProvider.retrieveUser(USER_NAME, USERNAME_PASSWORD_AUTHENTICATION_TOKEN);
        verify(authServerUserDetailsService).loadUserByUsername(USER_NAME);
    }

    @Test
    void willPerformAdditionalAuthenticationChecks() {
        when(passwordEncoder.matches(ENCODED_PASSWORD, USER_DETAILS.getPassword())).thenReturn(true);
        authenticationProvider.additionalAuthenticationChecks(USER_DETAILS, USERNAME_PASSWORD_AUTHENTICATION_TOKEN);
    }

    @Test
    void WillThrowBadCredentialsException_WhenCredentialIsNull() {
        assertThrows(BadCredentialsException.class,
                () -> authenticationProvider.additionalAuthenticationChecks(USER_DETAILS,
                        new UsernamePasswordAuthenticationToken(USER_DETAILS, null)));
    }

    @Test
    void WillThrowBadCredentialsException_WhenPasswordsDoNotMatch() {
        when(passwordEncoder.matches(ENCODED_PASSWORD, USER_DETAILS.getPassword())).thenReturn(false);
        assertThrows(BadCredentialsException.class,
                () -> authenticationProvider.additionalAuthenticationChecks(USER_DETAILS,
                        USERNAME_PASSWORD_AUTHENTICATION_TOKEN));
    }

    @Test
    void WillThrowDisabledException_WhenPasswordsDoNotMatch() {
        when(passwordEncoder.matches(ENCODED_PASSWORD, USER_DETAILS.getPassword())).thenReturn(true);

        assertThrows(DisabledException.class,
                () -> authenticationProvider.additionalAuthenticationChecks(
                        User.builder().username(USER_NAME).password(ENCODED_PASSWORD).roles().disabled(true).build(),
                        USERNAME_PASSWORD_AUTHENTICATION_TOKEN));
    }
}
