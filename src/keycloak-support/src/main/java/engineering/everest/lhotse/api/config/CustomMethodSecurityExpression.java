package engineering.everest.lhotse.api.config;

import java.util.UUID;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

@SuppressWarnings({ "rawtypes"})
public class CustomMethodSecurityExpression extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private final KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) this.authentication;
    private final KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();

    public CustomMethodSecurityExpression(Authentication authentication) {
        super(authentication);
    }

    public boolean memberOfOrg(UUID organizationId) {
        var otherClaims = principal.getKeycloakSecurityContext().getToken().getOtherClaims();
        return otherClaims != null && otherClaims.containsKey("organizationId")
                && organizationId.compareTo(UUID.fromString(otherClaims.get("organizationId").toString())) == 0;
    }

    @Override
    public void setFilterObject(Object o) {
        // Do nothing
    }

    @Override
    public Object getFilterObject() {
        return this;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        // Do nothing
    }

    @Override
    public Object getReturnObject() {
        return this;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
