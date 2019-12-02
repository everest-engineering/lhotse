package engineering.everest.starterkit.axon.command.validators;

import engineering.everest.starterkit.axon.command.validation.EmailAddressValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.Validates;
import org.apache.commons.lang3.Validate;
import org.apache.commons.validator.routines.EmailValidator;
import org.springframework.stereotype.Component;

@Component
public class EmailAddressValidator implements Validates<EmailAddressValidatableCommand> {

    @Override
    public void validate(EmailAddressValidatableCommand validatable) {
        if (validatable.getEmailAddress() == null) {
            return;
        }
        Validate.isTrue(EmailValidator.getInstance(false).isValid(validatable.getEmailAddress()), "Malformed email address");
    }
}
