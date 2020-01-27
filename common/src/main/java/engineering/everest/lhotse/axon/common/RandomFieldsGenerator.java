package engineering.everest.lhotse.axon.common;

import org.springframework.stereotype.Component;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.apache.commons.lang3.RandomStringUtils.random;

@Component
public class RandomFieldsGenerator {

    private static final int GENERATED_USERNAME_LENGTH = 30;
    private static final int GENERATED_PASSWORD_LENGTH = 40;

    public UUID genRandomUUID() {
        return randomUUID();
    }

    public String generateAccessToken() {
        return randomUUID().toString().replace("-", "");
    }

    public String generateUsername() {
        return random(GENERATED_USERNAME_LENGTH, true, true);
    }

    public String generatePassword() {
        return random(GENERATED_PASSWORD_LENGTH, true, true);
    }
}
