package engineering.everest.starterkit.axon.command.validators;

import engineering.everest.starterkit.axon.command.validation.UserUniqueEmailValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.Validates;
import engineering.everest.starterkit.users.services.UsersReadService;
import org.apache.commons.lang3.Validate;
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
        Validate.isTrue(!hasSameEmail, "Email address already exists");
    }
}
