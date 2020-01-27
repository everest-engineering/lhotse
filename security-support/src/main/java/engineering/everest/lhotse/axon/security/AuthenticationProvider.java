package engineering.everest.lhotse.axon.security;

import engineering.everest.lhotse.axon.security.userdetails.AuthServerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.AbstractUserDetailsAuthenticationProvider;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationProvider extends AbstractUserDetailsAuthenticationProvider {

    private final PasswordEncoder passwordEncoder;
    private final AuthServerUserDetailsService authServerUserDetailsService;

    @Autowired
    public AuthenticationProvider(PasswordEncoder passwordEncoder,
                                  AuthServerUserDetailsService authServerUserDetailsService) {
        super();
        this.passwordEncoder = passwordEncoder;
        this.authServerUserDetailsService = authServerUserDetailsService;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        if (authentication.getCredentials() == null) {
            logger.debug("Authentication failed: no credentials provided");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        String presentedPassword = authentication.getCredentials().toString();

        if (!passwordEncoder.matches(presentedPassword, userDetails.getPassword())) {
            logger.debug("Authentication failed: password does not match stored value");

            throw new BadCredentialsException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        if (!userDetails.isEnabled()) {
            logger.debug("Authentication failed: account is disabled");

            throw new DisabledException(messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.disabled",
                    "Account disabled"));
        }
    }

    @Override
    protected UserDetails retrieveUser(String username, UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {
        return authServerUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
