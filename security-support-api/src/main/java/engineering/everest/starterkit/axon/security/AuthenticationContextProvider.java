package engineering.everest.starterkit.axon.security;

import engineering.everest.starterkit.axon.common.domain.User;

public interface AuthenticationContextProvider {

    User getUser();
}
