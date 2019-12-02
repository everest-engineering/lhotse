package engineering.everest.starterkit.axon.security;

import engineering.everest.starterkit.axon.security.userdetails.AdminUserDetails;
import engineering.everest.starterkit.axon.common.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component
public class DefaultAuthenticationContextProvider implements AuthenticationContextProvider {

    @Override
    public User getUser() {
        Authentication authentication = getAuthentication();
        if (authentication instanceof OAuth2Authentication && authentication.isAuthenticated()) {
            return convert(authentication);
        }
        throw new AuthenticationFailureException("Authentication object is not valid");
    }

    private Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private User convert(Authentication authentication) {
        final var principal = authentication.getPrincipal();
        if (!(principal instanceof AdminUserDetails)) {
            throw new AuthenticationFailureException("Principle object is not valid");
        }
        return ((AdminUserDetails) principal).getUser();
    }
}
