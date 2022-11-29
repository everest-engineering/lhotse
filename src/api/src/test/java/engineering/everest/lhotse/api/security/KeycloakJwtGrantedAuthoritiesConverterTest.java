package engineering.everest.lhotse.api.security;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.shaded.json.JSONArray;
import com.nimbusds.jose.shaded.json.JSONObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;

import static com.nimbusds.jose.jwk.KeyUse.SIGNATURE;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

class KeycloakJwtGrantedAuthoritiesConverterTest {

    private static JWK jwk;

    private KeycloakJwtGrantedAuthoritiesConverter keycloakJwtGrantedAuthoritiesConverter;

    @BeforeAll
    static void beforeAll() throws NoSuchAlgorithmException {
        var gen = KeyPairGenerator.getInstance("RSA");
        gen.initialize(2048);
        var keyPair = gen.generateKeyPair();

        jwk = new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
            .privateKey((RSAPrivateKey) keyPair.getPrivate())
            .keyUse(SIGNATURE)
            .keyID(randomUUID().toString())
            .build();
    }

    @BeforeEach
    void setUp() {
        keycloakJwtGrantedAuthoritiesConverter = new KeycloakJwtGrantedAuthoritiesConverter();
    }

    @Test
    void convert_WillExtractRealmRoles() {
        var roles = new JSONArray();
        roles.add("first");
        roles.add("second");

        var realm_access = new JSONObject();
        realm_access.appendField("roles", roles);

        var claims = JwtClaimsSet.builder()
            .claim("realm_access", realm_access)
            .build();

        var expected = List.of(new SimpleGrantedAuthority("ROLE_FIRST"), new SimpleGrantedAuthority("ROLE_SECOND"));
        assertIterableEquals(expected, keycloakJwtGrantedAuthoritiesConverter.convert(createJwtFromClaims(claims)));
    }

    @Test
    void convert_WillExtractScopes() {
        var claims = JwtClaimsSet.builder()
            .claim("scope", "first second")
            .build();

        var expected = List.of(new SimpleGrantedAuthority("SCOPE_FIRST"), new SimpleGrantedAuthority("SCOPE_SECOND"));
        assertIterableEquals(expected, keycloakJwtGrantedAuthoritiesConverter.convert(createJwtFromClaims(claims)));
    }

    private static Jwt createJwtFromClaims(JwtClaimsSet claims) {
        return new NimbusJwtEncoder(new ImmutableJWKSet(new JWKSet(jwk))).encode(JwtEncoderParameters.from(claims));
    }
}
