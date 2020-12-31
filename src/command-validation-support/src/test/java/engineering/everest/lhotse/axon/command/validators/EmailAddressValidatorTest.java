package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailAddressValidatorTest {

    private EmailAddressValidator emailAddressValidator;

    @BeforeEach
    void setUp() {
        emailAddressValidator = new EmailAddressValidator();
    }

    @Test
    void validator_WillPass_WhenEmailLooksSwish() {
        emailAddressValidator.validate(() -> "perfectly@good.com");
    }

    @Test
    void validator_WillPass_WhenEmailIsNull() {
        emailAddressValidator.validate(() -> null);
    }

    @Test
    void validator_WillFail_WhenEmailIsBlank() {
        assertThrows(TranslatableIllegalArgumentException.class, () -> emailAddressValidator.validate(() -> ""));
    }

    @Test
    void validator_WillFail_WhenEmailIsAddressedLocally() {
        assertThrows(TranslatableIllegalArgumentException.class, () -> emailAddressValidator.validate(() -> "bob@localhost"));
    }

    @Test
    void validator_WillFail_WhenEmailContainsSpaces() {
        assertThrows(TranslatableIllegalArgumentException.class, () -> emailAddressValidator.validate(() -> "bob @ my.com"));
    }

    @Test
    void validator_WillFail_WhenEmailIsMissingHostname() {
        assertThrows(TranslatableIllegalArgumentException.class, () -> emailAddressValidator.validate(() -> "bob@"));
    }

    @Test
    void validator_WillFail_WhenHostnameIsAToplevelDomain() {
        assertThrows(TranslatableIllegalArgumentException.class, () -> emailAddressValidator.validate(() -> "bob@engineering"));
    }
}
