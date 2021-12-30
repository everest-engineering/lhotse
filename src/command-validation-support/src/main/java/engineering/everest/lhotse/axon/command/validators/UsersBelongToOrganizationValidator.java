package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
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
    private final AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @Autowired
    public UsersBelongToOrganizationValidator(UsersReadService usersReadService,
                                              AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory) {
        this.usersReadService = usersReadService;
        this.axonCommandExecutionExceptionFactory = axonCommandExecutionExceptionFactory;
    }

    @Override
    @SuppressWarnings("PMD.AvoidInstantiatingObjectsInLoops")
    public void validate(UsersBelongToOrganizationValidatableCommand validatable) {
        for (UUID userId : validatable.getUserIds()) {
            var userOrganizationId = usersReadService.getById(userId).getOrganizationId();
            if (!validatable.getOrganizationId().equals(userOrganizationId)) {
                axonCommandExecutionExceptionFactory.throwWrappedInCommandExecutionException(
                    new TranslatableIllegalArgumentException(USER_NOT_MEMBER_OF_ORGANIZATION));
            }
        }
    }
}
