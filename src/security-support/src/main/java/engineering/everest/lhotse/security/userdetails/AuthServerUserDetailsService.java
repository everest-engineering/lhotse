package engineering.everest.lhotse.security.userdetails;

import engineering.everest.lhotse.users.authserver.AuthServerUser;
import engineering.everest.lhotse.users.authserver.AuthServerUserReadService;
import engineering.everest.starterkit.security.AuthenticationServerUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AuthServerUserDetailsService implements AuthenticationServerUserDetailsService {

    private final AuthServerUserReadService authServerUserReadService;

    @Autowired
    public AuthServerUserDetailsService(AuthServerUserReadService authServerUserReadService) {
        this.authServerUserReadService = authServerUserReadService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthServerUser authServerUser;
        try {
            authServerUser = authServerUserReadService.getByUsername(username);
        } catch (NoSuchElementException e) {
            throw new UsernameNotFoundException("Invalid credentials", e);
        }
        return org.springframework.security.core.userdetails.User.builder()
                .username(authServerUser.getUsername())
                .password(authServerUser.getEncodedPassword())
                .disabled(authServerUser.isDisabled())
                .roles()
                .build();

    }
}
