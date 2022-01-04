package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validation.UserUniqueEmailValidatableCommand;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.commandhandling.CommandExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({ MockitoExtension.class })
public class UserUniqueEmailValidatorTest {

    private static final String EXISTING_USER_EMAIL_1 = "testEmail1@test.com";
    private static final String NEW_USER_EMAIL = "newUser@test.com";

    @Mock
    public UsersReadService usersReadService;

    private UsersUniqueEmailValidator usersUniqueEmailValidator;

    @BeforeEach
    void setUp() {
        usersUniqueEmailValidator = new UsersUniqueEmailValidator(usersReadService, new AxonCommandExecutionExceptionFactory());
    }

    @Test
    void validate_WillFail_WhenUserWithEmailAlreadyExists() {
        when(usersReadService.hasUserWithEmail(EXISTING_USER_EMAIL_1)).thenReturn(true);

        var exception = assertThrows(CommandExecutionException.class,
            () -> usersUniqueEmailValidator.validate((UserUniqueEmailValidatableCommand) () -> EXISTING_USER_EMAIL_1));
        assertEquals("EMAIL_ADDRESS_ALREADY_EXISTS", exception.getMessage());

        var translatableException = (TranslatableIllegalStateException) exception.getDetails().orElseThrow();
        assertEquals("EMAIL_ADDRESS_ALREADY_EXISTS", translatableException.getMessage());
    }

    @Test
    void validate_WillPass_WhenUserNameWithEmailDoesNotExist() {
        when(usersReadService.hasUserWithEmail(NEW_USER_EMAIL)).thenReturn(false);

        usersUniqueEmailValidator.validate((UserUniqueEmailValidatableCommand) () -> NEW_USER_EMAIL);
    }
}
