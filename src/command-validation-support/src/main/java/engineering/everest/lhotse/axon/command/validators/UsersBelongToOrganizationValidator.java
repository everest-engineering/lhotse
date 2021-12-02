package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.UsersBelongToOrganizationValidatableCommand;
import engineering.everest.lhotse.axon.command.validation.Validates;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static engineering.everest.lhotse.i18n.MessageKeys.USER_NOT_MEMBER_OF_ORGANIZATION;

@Component
public class UsersBelongToOrganizationValidator implements Validates<UsersBelongToOrganizationValidatableCommand> {

    private final UsersReadService usersReadService;

    @Autowired
    public UsersBelongToOrganizationValidator(UsersReadService usersReadService) {
        this.usersReadService = usersReadService;
    }

    @Override
    public void validate(UsersBelongToOrganizationValidatableCommand validatable) {
        for (UUID userId : validatable.getUserIds()) {
            var userOrganizationId = usersReadService.getById(userId).getOrganizationId();
            if (!validatable.getOrganizationId().equals(userOrganizationId)) {
                throw new TranslatableIllegalArgumentException(USER_NOT_MEMBER_OF_ORGANIZATION);
            }
        }
    }
}
