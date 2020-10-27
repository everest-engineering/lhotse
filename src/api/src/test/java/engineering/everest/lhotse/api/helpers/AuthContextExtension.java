package engineering.everest.lhotse.api.helpers;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

public class AuthContextExtension implements BeforeEachCallback {
    @Override
    public void beforeEach(ExtensionContext context) {
        MockAuthenticationContextProvider.userHolder.remove();
        MockAuthenticationContextProvider.getAuthUser();
    }
}
