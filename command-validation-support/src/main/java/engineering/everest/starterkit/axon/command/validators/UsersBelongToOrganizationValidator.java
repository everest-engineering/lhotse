package engineering.everest.starterkit.axon.command.validators;

import engineering.everest.starterkit.axon.command.validation.UsersBelongToOrganizationValidatableCommand;
import engineering.everest.starterkit.axon.command.validation.Validates;
import engineering.everest.starterkit.axon.users.services.UsersReadService;
import org.apache.commons.lang3.Validate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.UUID;

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
            Validate.isTrue(validatable.getOrganizationId().equals(usersReadService.getById(userId).getOrganizationId()),
                    "User %s does not belong to organization", userId);
        }
    }
}
