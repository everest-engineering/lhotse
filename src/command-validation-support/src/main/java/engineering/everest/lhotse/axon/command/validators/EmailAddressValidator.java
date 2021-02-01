package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.TranslatableExceptionFactory;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

import static engineering.everest.lhotse.i18n.MessageKeys.EMAIL_ADDRESS_MALFORMED;

@Component
public class EmailAddressValidator implements Validates<EmailAddressValidatableCommand> {

    @Override
    public void validate(EmailAddressValidatableCommand validatable) {
        if (validatable.getEmailAddress() == null) {
            return;
        }
        if (!EmailValidator.getInstance(false).isValid(validatable.getEmailAddress())) {
            TranslatableExceptionFactory.throwForKey(EMAIL_ADDRESS_MALFORMED);
        }
    }
}
