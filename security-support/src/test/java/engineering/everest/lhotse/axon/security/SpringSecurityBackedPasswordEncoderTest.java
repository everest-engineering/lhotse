package engineering.everest.lhotse.axon.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.security.SecureRandom;

import static org.junit.jupiter.api.Assertions.*;

class SpringSecurityBackedPasswordEncoderTest {

    private static final String RAW_PASSWORD = "raw-password";

    private SpringSecurityBackedPasswordEncoder springSecurityBackedPasswordEncoder;

    @BeforeEach
    void setUp() {
        springSecurityBackedPasswordEncoder = new SpringSecurityBackedPasswordEncoder(
                new BCryptPasswordEncoder(10, new SecureRandom()));
    }

    @Test
    void encodeIsNondeterministic() {
        assertNotEquals(springSecurityBackedPasswordEncoder.encode(RAW_PASSWORD), springSecurityBackedPasswordEncoder.encode(RAW_PASSWORD));
    }

    @Test
    void canBeUsedToAuthenticateUsers() {
        String encodedPassword = springSecurityBackedPasswordEncoder.encode(RAW_PASSWORD);
        assertTrue(springSecurityBackedPasswordEncoder.matches(RAW_PASSWORD, encodedPassword));
    }
}