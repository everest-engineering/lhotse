package engineering.everest.lhotse.axon.security.userdetails;

import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.starterkit.security.ApplicationUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;

@Service
public class AppUserDetailsService implements ApplicationUserDetailsService {

    private final UsersReadService usersReadService;

    public AppUserDetailsService(UsersReadService usersReadService) {
        this.usersReadService = usersReadService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user;
        try {
            user = usersReadService.getUserByUsername(username);
        } catch (NoSuchElementException e) {
            throw new UsernameNotFoundException("Invalid credentials", e);
        }
        return new AppUserDetails(user);
    }
}
