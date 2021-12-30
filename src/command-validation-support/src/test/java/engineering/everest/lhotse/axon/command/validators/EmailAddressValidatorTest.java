package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import org.axonframework.commandhandling.CommandExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EmailAddressValidatorTest {

    private EmailAddressValidator emailAddressValidator;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @BeforeEach
    void setUp() {
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();
        emailAddressValidator = new EmailAddressValidator(axonCommandExecutionExceptionFactory);
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
        var exception = assertThrows(CommandExecutionException.class, () -> emailAddressValidator.validate(() -> ""));
        assertEquals("EMAIL_ADDRESS_MALFORMED", exception.getMessage());

        var translatableIllegalArgumentException = (TranslatableIllegalArgumentException) exception.getDetails().orElseThrow();
        assertEquals("EMAIL_ADDRESS_MALFORMED", translatableIllegalArgumentException.getMessage());
    }

    @Test
    void validator_WillFail_WhenEmailIsAddressedLocally() {
        var exception = assertThrows(CommandExecutionException.class, () -> emailAddressValidator.validate(() -> "bob@localhost"));
        assertEquals("EMAIL_ADDRESS_MALFORMED", exception.getMessage());

        var translatableIllegalArgumentException = (TranslatableIllegalArgumentException) exception.getDetails().orElseThrow();
        assertEquals("EMAIL_ADDRESS_MALFORMED", translatableIllegalArgumentException.getMessage());
    }

    @Test
    void validator_WillFail_WhenEmailContainsSpaces() {
        var exception = assertThrows(CommandExecutionException.class, () -> emailAddressValidator.validate(() -> "bob @ my.com"));
        assertEquals("EMAIL_ADDRESS_MALFORMED", exception.getMessage());

        var translatableIllegalArgumentException = (TranslatableIllegalArgumentException) exception.getDetails().orElseThrow();
        assertEquals("EMAIL_ADDRESS_MALFORMED", translatableIllegalArgumentException.getMessage());
    }

    @Test
    void validator_WillFail_WhenEmailIsMissingHostname() {
        var exception = assertThrows(CommandExecutionException.class, () -> emailAddressValidator.validate(() -> "bob@"));
        assertEquals("EMAIL_ADDRESS_MALFORMED", exception.getMessage());

        var translatableIllegalArgumentException = (TranslatableIllegalArgumentException) exception.getDetails().orElseThrow();
        assertEquals("EMAIL_ADDRESS_MALFORMED", translatableIllegalArgumentException.getMessage());
    }

    @Test
    void validator_WillFail_WhenHostnameIsAToplevelDomain() {
        var exception = assertThrows(CommandExecutionException.class, () -> emailAddressValidator.validate(() -> "bob@engineering"));
        assertEquals("EMAIL_ADDRESS_MALFORMED", exception.getMessage());

        var translatableIllegalArgumentException = (TranslatableIllegalArgumentException) exception.getDetails().orElseThrow();
        assertEquals("EMAIL_ADDRESS_MALFORMED", translatableIllegalArgumentException.getMessage());
    }
}
