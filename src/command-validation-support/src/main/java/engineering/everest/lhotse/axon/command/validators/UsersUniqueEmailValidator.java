package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static engineering.everest.lhotse.i18n.MessageKeys.EMAIL_ADDRESS_ALREADY_EXISTS;

@Component
public class UsersUniqueEmailValidator implements Validates<UserUniqueEmailValidatableCommand> {

    private final UsersReadService usersReadService;
    private final AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @Autowired
    public UsersUniqueEmailValidator(UsersReadService usersReadService,
                                     AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        this.usersReadService = usersReadService;
        this.axonCommandExecutionExceptionFactory = axonCommandExecutionExceptionFactory;
    }

    @Override
    public void validate(UserUniqueEmailValidatableCommand command) {
        if (usersReadService.hasUserWithEmail(command.getEmailAddress())) {
            axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                new TranslatableIllegalStateException(EMAIL_ADDRESS_ALREADY_EXISTS));
        }
    }
}
