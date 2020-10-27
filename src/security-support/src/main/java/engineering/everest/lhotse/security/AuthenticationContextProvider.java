package engineering.everest.lhotse.security;

import engineering.everest.lhotse.security.userdetails.AppUserDetails;
import engineering.everest.lhotse.axon.common.domain.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationContextProvider {

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
        if (!(principal instanceof AppUserDetails)) {
            throw new AuthenticationFailureException("Principal object is not valid");
        }
        return ((AppUserDetails) principal).getUser();
    }
}
