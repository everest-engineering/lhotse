package engineering.everest.lhotse.axon.security;

import engineering.everest.lhotse.axon.common.domain.User;

public interface AuthenticationContextProvider {

    User getUser();
}
