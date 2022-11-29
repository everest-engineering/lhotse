package engineering.everest.lhotse.api.security;

import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

@Component
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var authorities = extractRealms(jwt);
        authorities.addAll(extractScopes(jwt));
        return authorities;
    }

    private static List<GrantedAuthority> extractRealms(Jwt jwt) {
        var grantedAuthorities = new ArrayList<GrantedAuthority>();

        if (jwt.hasClaim("realm_access")) {
            var realmAccess = (JSONObject) jwt.getClaims().get("realm_access");
            if (realmAccess.containsKey("roles")) {
                var realmRoles = (JSONArray) realmAccess.get("roles");
                for (var realmRole : realmRoles) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + realmRole.toString().toUpperCase(Locale.getDefault())));
                }
            }
        }

        return grantedAuthorities;
    }

    private static List<GrantedAuthority> extractScopes(Jwt jwt) {
        var grantedAuthorities = new ArrayList<GrantedAuthority>();

        if (jwt.hasClaim("scope")) {
            var scope = (String) jwt.getClaims().get("scope");
            if (!scope.isBlank() && !scope.isEmpty()) {
                var scopes = scope.split("\\s");
                for (var scopeAuthority : scopes) {
                    grantedAuthorities.add(new SimpleGrantedAuthority("SCOPE_" + scopeAuthority.toUpperCase(Locale.getDefault())));
                }
            }
        }

        return grantedAuthorities;
    }
}
