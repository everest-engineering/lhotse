package engineering.everest.starterkit.api.rest.security;

import engineering.everest.starterkit.axon.common.domain.Identifiable;
import engineering.everest.starterkit.axon.common.services.ReadService;
import engineering.everest.starterkit.axon.common.services.ReadServiceProvider;
import engineering.everest.starterkit.axon.security.AuthenticationContextProvider;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Locale;
import java.util.UUID;

@Component
public class EntityPermissionEvaluator implements PermissionEvaluator {

    private final AuthenticationContextProvider authenticationContextProvider;
    private final ReadServiceProvider readServiceProvider;

    public EntityPermissionEvaluator(AuthenticationContextProvider authenticationContextProvider,
                                     ReadServiceProvider readServiceProvider) {
        this.authenticationContextProvider = authenticationContextProvider;
        this.readServiceProvider = readServiceProvider;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        throw new UnsupportedOperationException("NYI");
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        ReadService<? extends Identifiable> service = readServiceProvider.getService(targetType);
        if (service == null) {
            throw new RuntimeException(String.format("Cannot find ReadService for: %s", targetType));
        }

        var identifiable = service.getById((UUID) targetId);
        var user = authenticationContextProvider.getUser();

        switch (((String) permission).toLowerCase(Locale.getDefault())) {
            case "read":
                return identifiable.canRead(user);
            case "create":
                return identifiable.canCreate(user);
            case "update":
                return identifiable.canUpdate(user);
            case "delete":
                return identifiable.canDelete(user);
            default:
                throw new IllegalArgumentException(String.format("Unknown permission: %s", permission));
        }
    }
}
