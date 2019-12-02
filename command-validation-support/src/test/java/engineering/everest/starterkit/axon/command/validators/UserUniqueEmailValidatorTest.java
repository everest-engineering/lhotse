package engineering.everest.starterkit.axon.command.validators;

import engineering.everest.starterkit.axon.command.validation.UserUniqueEmailValidatableCommand;
import engineering.everest.starterkit.axon.users.services.UsersReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class})
public class UserUniqueEmailValidatorTest {

    private static final String EXISTING_USER_EMAIL_1 = "testEmail1@test.com";
    private static final String NEW_USER_EMAIL = "newUser@test.com";

    @Mock
    public UsersReadService usersReadService;

    private UsersUniqueEmailValidator usersUniqueEmailValidator;

    @BeforeEach
    void setUp() {
        usersUniqueEmailValidator = new UsersUniqueEmailValidator(usersReadService);
    }

    @Test
    void validate_willFail_WhenUserWithEmailAlreadyExists() {
        when(usersReadService.hasUserWithEmail(EXISTING_USER_EMAIL_1)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            usersUniqueEmailValidator.validate((UserUniqueEmailValidatableCommand) () -> EXISTING_USER_EMAIL_1);
        });
    }

    @Test
    void validate_willPass_WhenUserNameWithEmailDoesNotExist() {
        when(usersReadService.hasUserWithEmail(NEW_USER_EMAIL)).thenReturn(false);

        usersUniqueEmailValidator.validate((UserUniqueEmailValidatableCommand) () -> NEW_USER_EMAIL);
    }
}
