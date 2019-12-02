package engineering.everest.starterkit.axon.security.userdetails;

import engineering.everest.starterkit.users.services.UsersReadService;
import engineering.everest.starterkit.axon.common.domain.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component("adminUserDetailsService")
public class AdminUserDetailsService implements UserDetailsService {

    private final UsersReadService usersReadService;

    public AdminUserDetailsService(UsersReadService usersReadService) {
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
        return new AdminUserDetails(user);
    }
}
