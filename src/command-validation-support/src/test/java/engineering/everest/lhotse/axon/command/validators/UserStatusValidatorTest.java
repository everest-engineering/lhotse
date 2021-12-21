package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.UsersStatusValidatableCommand;
import engineering.everest.lhotse.common.domain.User;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalStateException;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStatusValidatorTest {

    private final static UUID USER_ID = UUID.randomUUID();

    private UserStatusValidator userStatusValidator;

    @Mock
    private UsersReadService usersReadService;
    @Mock
    private User user;

    @BeforeEach
    void setUp() {
        userStatusValidator = new UserStatusValidator(usersReadService);

        lenient().when(usersReadService.exists(USER_ID)).thenReturn(true);
        lenient().when(usersReadService.getById(USER_ID)).thenReturn(user);
        lenient().when(user.isDisabled()).thenReturn(false);
    }

    @Test
    void validate_WillFail_WhenUserDoesNotExist() {
        when(usersReadService.exists(USER_ID)).thenReturn(false);

        var thrownException = assertThrows(TranslatableIllegalStateException.class,
            () -> userStatusValidator.validate((UsersStatusValidatableCommand) () -> Set.of(USER_ID)));
        assertEquals("USER_IS_UNKNOWN", thrownException.getMessage());
    }

    @Test
    void validate_WillFail_WhenUserIsDisabled() {
        when(user.isDisabled()).thenReturn(true);

        var thrownException = assertThrows(TranslatableIllegalStateException.class,
            () -> userStatusValidator.validate((UsersStatusValidatableCommand) () -> Set.of(USER_ID)));
        assertEquals("USER_IS_DISABLED", thrownException.getMessage());
    }

    @Test
    void validate_WillPass_WhenUserExistsAndIsEnabled() {
        userStatusValidator.validate((UsersStatusValidatableCommand) () -> Set.of(USER_ID));
    }
}
