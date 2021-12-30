package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

import static engineering.everest.lhotse.i18n.MessageKeys.EMAIL_ADDRESS_MALFORMED;

@Component
public class EmailAddressValidator implements Validates<EmailAddressValidatableCommand> {

    private final AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    public EmailAddressValidator(AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        this.axonCommandExecutionExceptionFactory = axonCommandExecutionExceptionFactory;
    }

    @Override
    public void validate(EmailAddressValidatableCommand validatable) {
        if (validatable.getEmailAddress() == null) {
            return;
        }
        if (!EmailValidator.getInstance(false).isValid(validatable.getEmailAddress())) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalArgumentException(EMAIL_ADDRESS_MALFORMED));
        }
    }
}
