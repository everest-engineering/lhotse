package engineering.everest.starterkit.axon.security.userdetails;

import engineering.everest.starterkit.axon.common.domain.Role;
import engineering.everest.starterkit.axon.common.domain.User;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Delegate;
import org.springframework.security.core.userdetails.UserDetails;

@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AdminUserDetails implements UserDetails {

    @Getter
    @EqualsAndHashCode.Include
    private final User user;

    @Delegate(types = UserDetails.class)
    private final UserDetails userDetails;

    public AdminUserDetails(User user) {
        this.user = user;
        this.userDetails = org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password("")
                .disabled(user.isDisabled())
                .roles(user.getRoles().stream().map(Role::toString).toArray(String[]::new))
                .build();
    }

}
