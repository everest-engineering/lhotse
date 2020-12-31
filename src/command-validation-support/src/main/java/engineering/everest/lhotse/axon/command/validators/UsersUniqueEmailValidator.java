package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.TranslatingValidator;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UsersUniqueEmailValidator implements Validates<UserUniqueEmailValidatableCommand> {
    private final UsersReadService usersReadService;

    @Autowired
    public UsersUniqueEmailValidator(UsersReadService usersReadService) {
        this.usersReadService = usersReadService;
    }

    @Override
    public void validate(UserUniqueEmailValidatableCommand command) {
        boolean hasSameEmail = usersReadService.hasUserWithEmail(command.getEmailAddress());
        TranslatingValidator.isTrue(!hasSameEmail, "EMAIL_ADDRESS_ALREADY_EXISTS");
    }
}
