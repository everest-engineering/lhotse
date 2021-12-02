package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.UsersStatusValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.USER_IS_DISABLED;
import static engineering.everest.lhotse.i18n.MessageKeys.USER_IS_UNKNOWN;

@Component
public class UserStatusValidator implements Validates<UsersStatusValidatableCommand> {

    private final UsersReadService usersReadService;

    public UserStatusValidator(UsersReadService usersReadService) {
        this.usersReadService = usersReadService;
    }

    @Override
    public void validate(UsersStatusValidatableCommand validatable) {
        for (UUID userId : validatable.getUserIds()) {
            if (!usersReadService.exists(userId)) {
                throw new TranslatableIllegalStateException(USER_IS_UNKNOWN, userId);
            }
            if (usersReadService.getById(userId).isDisabled()) {
                throw new TranslatableIllegalStateException(USER_IS_DISABLED, userId);
            }
        }
    }
}
