package engineering.everest.lhotse.axon.common;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.random;

@Component
public class RandomFieldsGenerator {
    private static final int GENERATED_PASSWORD_LENGTH = 40;
    private static final char[] PASSWORD_CHARACTER_SET =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[];:,.".toCharArray();

    private final SecureRandom secureRandom;

    public RandomFieldsGenerator() {
        this.secureRandom = new SecureRandom();
    }

    public UUID genRandomUUID() {
        return randomUUID();
    }

    public String generatePassword() {
        return random(GENERATED_PASSWORD_LENGTH, 0, PASSWORD_CHARACTER_SET.length - 1,
            false, false, PASSWORD_CHARACTER_SET, secureRandom);
    }
}
