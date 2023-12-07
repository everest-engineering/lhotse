package engineering.everest.lhotse.common;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFacade {

    public String getRequestingUserId() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}