package engineering.everest.lhotse.common;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RandomFieldsGeneratorTest {
    private final static char[] ALPHABET_CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
    private final static char[] INTEGER_CHARACTERS = "0123456789".toCharArray();
    private final static char[] SPECIAL_CHARACTERS = "~`!@#$%^&*()-_=+[];:,.".toCharArray();

    private RandomFieldsGenerator randomFieldsGenerator;

    @BeforeEach
    void setUp() {
        randomFieldsGenerator = new RandomFieldsGenerator();
    }

    @Test
    void generatePassword_WillCreateAlphanumericAndSpecialLetterPasswords() {
        boolean alphaFound = false;
        boolean numericFound = false;
        boolean specialFound = false;

        for (int i = 0; i < 100; i++) {
            var password = randomFieldsGenerator.generatePassword();
            alphaFound |= StringUtils.containsAny(password, ALPHABET_CHARACTERS);
            numericFound |= StringUtils.containsAny(password, INTEGER_CHARACTERS);
            specialFound |= StringUtils.containsAny(password, SPECIAL_CHARACTERS);
        }

        assertTrue(alphaFound);
        assertTrue(numericFound);
        assertTrue(specialFound);
    }
}
