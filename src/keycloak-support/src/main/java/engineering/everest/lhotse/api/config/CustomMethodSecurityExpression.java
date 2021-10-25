package engineering.everest.lhotse.api.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.keycloak.KeycloakPrincipal;
import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

import engineering.everest.lhotse.axon.common.domain.Role;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class CustomMethodSecurityExpression extends SecurityExpressionRoot
        implements MethodSecurityExpressionOperations {

    private KeycloakAuthenticationToken token = (KeycloakAuthenticationToken) this.authentication;
    private KeycloakPrincipal principal = (KeycloakPrincipal) token.getPrincipal();

    public CustomMethodSecurityExpression(Authentication authentication) {
        super(authentication);
    }

    public boolean hasCustomRole(String role) {
        var otherClaims = principal.getKeycloakSecurityContext().getToken().getOtherClaims();
        var roles = new HashSet<Role>();

        if (otherClaims != null && otherClaims.containsKey("roles")) {
            var rolesObject = otherClaims.get("roles");
            if (rolesObject instanceof Set) {
                roles.addAll((Set<Role>) rolesObject);
            } else {
                roles.addAll((ArrayList<Role>) rolesObject);
            }
        }

        return Arrays.stream(roles.toArray()).anyMatch(v -> v.equals(role));
    }

    public boolean belongsToOrg(UUID organizationId) {
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
