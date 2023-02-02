package engineering.everest.lhotse.api.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

@Component
public class KeycloakJwtGrantedAuthoritiesConverter implements Converter<Jwt, Collection<GrantedAuthority>> {

    private final Locale defaultLocale;

    public KeycloakJwtGrantedAuthoritiesConverter() {
        this.defaultLocale = Locale.getDefault();
    }

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        var authorities = extractRealms(jwt);
        authorities.addAll(extractScopes(jwt));
        return authorities;
    }

    private List<GrantedAuthority> extractRealms(Jwt jwt) {
        if (jwt.hasClaim("realm_access")) {
            var realmAccess = (Map<String, Object>) jwt.getClaims().get("realm_access");
            var roles = (List<String>) realmAccess.get("roles");
            return roles.stream()
                .map(name -> "ROLE_" + name.toUpperCase(defaultLocale))
                .map(SimpleGrantedAuthority::new)
                .collect(toList());
        }
        return new ArrayList<>();
    }

    private List<GrantedAuthority> extractScopes(Jwt jwt) {
        if (jwt.hasClaim("scope")) {
            var scope = (String) jwt.getClaims().get("scope");
            if (!scope.isBlank() && !scope.isEmpty()) {
                var scopes = scope.split("\\s");
                return stream(scopes)
                    .map(name -> "SCOPE_" + name.toUpperCase(defaultLocale))
                    .map(SimpleGrantedAuthority::new)
                    .collect(toList());
            }
        }
        return new ArrayList<>();
    }
}
