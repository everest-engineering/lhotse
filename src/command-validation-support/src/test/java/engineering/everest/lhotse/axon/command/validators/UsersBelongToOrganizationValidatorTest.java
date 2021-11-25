package engineering.everest.lhotse.axon.command.validators;

import engineering.everest.lhotse.axon.command.validation.UsersBelongToOrganizationValidatableCommand;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
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

@ExtendWith(MockitoExtension.class)
class UsersBelongToOrganizationValidatorTest {

    private static final UUID USER_ID_1 = UUID.randomUUID();
    private static final UUID USER_ID_2 = UUID.randomUUID();
    private static final UUID USER_ID_3 = UUID.randomUUID();
    private static final UUID ORGANIZATION_ID_1 = UUID.randomUUID();
    private static final UUID ORGANIZATION_ID_2 = UUID.randomUUID();
    private static final User ORG_1_USER_1 = new User(USER_ID_1, ORGANIZATION_ID_1, "username-1", "user-display-1");
    private static final User ORG_1_USER_2 = new User(USER_ID_2, ORGANIZATION_ID_1, "username-2", "user-display-2");
    private static final User ORG_2_USER_1 = new User(USER_ID_3, ORGANIZATION_ID_2, "username-3", "user-display-3");

    private UsersBelongToOrganizationValidator usersBelongToOrganizationValidator;

    @Mock
    private UsersReadService usersReadService;

    @BeforeEach
    void setUp() {
        usersBelongToOrganizationValidator = new UsersBelongToOrganizationValidator(usersReadService);

        lenient().when(usersReadService.getById(USER_ID_1)).thenReturn(ORG_1_USER_1);
        lenient().when(usersReadService.getById(USER_ID_2)).thenReturn(ORG_1_USER_2);
        lenient().when(usersReadService.getById(USER_ID_3)).thenReturn(ORG_2_USER_1);
    }

    @Test
    void validate_WillPass_WhenEveryUserBelongsToOrganization() {
        usersBelongToOrganizationValidator.validate(createValidatableCommand(Set.of(USER_ID_1, USER_ID_2)));
    }

    @Test
    void validate_WillFail_WhenAnyEveryUserBelongsToDifferentOrganization() {
        var thrownException = assertThrows(TranslatableIllegalArgumentException.class, () ->
                usersBelongToOrganizationValidator.validate(createValidatableCommand(Set.of(USER_ID_1, USER_ID_2, USER_ID_3))));
        assertEquals("USER_NOT_MEMBER_OF_ORGANIZATION", thrownException.getMessage());
    }

    private UsersBelongToOrganizationValidatableCommand createValidatableCommand(Set<UUID> userIds) {
        return new UsersBelongToOrganizationValidatableCommand() {
            @Override
            public UUID getOrganizationId() {
                return ORGANIZATION_ID_1;
            }

            @Override
            public Set<UUID> getUserIds() {
                return userIds;
            }
        };
    }
}