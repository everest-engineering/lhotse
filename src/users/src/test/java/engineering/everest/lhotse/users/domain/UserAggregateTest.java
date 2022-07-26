package engineering.everest.lhotse.users.domain;

import engineering.everest.lhotse.axon.command.AxonCommandExecutionExceptionFactory;
import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import engineering.everest.lhotse.axon.command.validators.UserStatusValidator;
import engineering.everest.lhotse.axon.command.validators.UsersUniqueEmailValidator;
import engineering.everest.lhotse.users.domain.commands.AddUserRolesCommand;
import engineering.everest.lhotse.users.domain.commands.CreateOrganizationUserCommand;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.DeleteAndForgetUserCommand;
import engineering.everest.lhotse.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.lhotse.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.commandhandling.CommandExecutionException;
import org.axonframework.eventsourcing.AggregateDeletedException;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolationException;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class UserAggregateTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID PROFILE_PHOTO_FILE_ID = randomUUID();
    private static final String EMAIL_ADDRESS = "user@email.com";
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String DISPLAY_NAME_CHANGE = "display-name-change";
    private static final String EMAIL_ADDRESS_CHANGE = "email@change.com";
    private static final String NO_CHANGE = null;
    private static final String BLANK_FIELD = "";

    private static final UserDeletedAndForgottenEvent USER_DELETED_AND_FORGOTTEN_EVENT =
        new UserDeletedAndForgottenEvent(USER_ID, ADMIN_ID, "It's the right thing to do");
    private static final UserCreatedForNewlyRegisteredOrganizationEvent USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT =
        new UserCreatedForNewlyRegisteredOrganizationEvent(ORGANIZATION_ID, USER_ID, USER_DISPLAY_NAME, EMAIL_ADDRESS);

    private FixtureConfiguration<UserAggregate> testFixture;
    private AxonCommandExecutionExceptionFactory axonCommandExecutionExceptionFactory;

    @Mock
    private UsersReadService usersReadService;
    @Mock
    private EmailAddressValidator emailAddressValidator;
    @Mock
    private UsersUniqueEmailValidator usersUniqueEmailValidator;
    @Mock
    private UserStatusValidator userStatusValidator;

    @BeforeEach
    void setUp() {
        axonCommandExecutionExceptionFactory = new AxonCommandExecutionExceptionFactory();

        testFixture = new AggregateTestFixture<>(UserAggregate.class)
            .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor(
                emailAddressValidator,
                usersUniqueEmailValidator,
                userStatusValidator))
            .registerInjectableResource(usersReadService)
            .registerInjectableResource(axonCommandExecutionExceptionFactory);
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var organizationClass = UserAggregate.class;
        var aggregateAnnotation = organizationClass.getAnnotation(Aggregate.class);
        assertEquals("userAggregateSnapshotTriggerDefinition", aggregateAnnotation.snapshotTriggerDefinition());
    }

    @Test
    void createOrganizationUserCommandEmits_WhenAllMandatoryFieldsArePresentInCreationCommand() {
        testFixture.givenNoPriorActivity()
            .when(new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, EMAIL_ADDRESS, USER_DISPLAY_NAME))
            .expectEvents(new UserCreatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID, USER_DISPLAY_NAME, EMAIL_ADDRESS));
    }

    @Test
    void createUserForNewlyRegisteredOrganizationCommandEmits_WhenAllMandatoryFieldsArePresentInCreationCommand() {
        testFixture.givenNoPriorActivity()
            .when(new CreateUserForNewlyRegisteredOrganizationCommand(ORGANIZATION_ID, USER_ID, EMAIL_ADDRESS, USER_DISPLAY_NAME))
            .expectEvents(new UserCreatedForNewlyRegisteredOrganizationEvent(ORGANIZATION_ID, USER_ID, USER_DISPLAY_NAME, EMAIL_ADDRESS));
    }

    @Test
    void rejectsCreateOrganizationUserCommand_WhenEmailValidatorFails() {
        var command = new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, null, USER_DISPLAY_NAME);

        testFixture.givenNoPriorActivity()
            .when(command)
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserForNewlyRegisteredOrganizationCommand_WhenEmailValidatorFails() {
        testFixture.givenNoPriorActivity()
            .when(new CreateUserForNewlyRegisteredOrganizationCommand(ORGANIZATION_ID, USER_ID, null, USER_DISPLAY_NAME))
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateOrganizationUserCommand_WhenDisplayNameIsBlank() {
        testFixture.givenNoPriorActivity()
            .when(new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, EMAIL_ADDRESS, ""))
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserForNewlyRegisteredOrganizationCommand_WhenDisplayNameIsBlank() {
        testFixture.givenNoPriorActivity()
            .when(new CreateUserForNewlyRegisteredOrganizationCommand(ORGANIZATION_ID, USER_ID, EMAIL_ADDRESS, ""))
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateOrganizationUserCommand_WhenDisplayNameIsNull() {
        testFixture.givenNoPriorActivity()
            .when(new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, EMAIL_ADDRESS, null))
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserForNewlyRegisteredOrganizationCommand_WhenDisplayNameIsNull() {
        testFixture.givenNoPriorActivity()
            .when(new CreateUserForNewlyRegisteredOrganizationCommand(ORGANIZATION_ID, USER_ID, EMAIL_ADDRESS, null))
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateOrganizationUserCommand_WhenRequestingUserIsNull() {
        var command = new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, null, EMAIL_ADDRESS, USER_DISPLAY_NAME);

        testFixture.givenNoPriorActivity()
            .when(command)
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserForNewlyRegisteredOrganizationCommand_WhenRequestingUserIsNull() {
        testFixture.givenNoPriorActivity()
            .when(new CreateUserForNewlyRegisteredOrganizationCommand(ORGANIZATION_ID, null, EMAIL_ADDRESS, USER_DISPLAY_NAME))
            .expectNoEvents()
            .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateOrganizationUserCommand_WhenUniqueUserEmailValidatorFails() {
        var command = new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, EMAIL_ADDRESS, USER_DISPLAY_NAME);
        doThrow(IllegalArgumentException.class).when(usersUniqueEmailValidator).validate(command);

        testFixture.givenNoPriorActivity()
            .when(command)
            .expectNoEvents()
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void createUserForNewlyRegisteredOrganizationCommandEmits() {
        testFixture.givenNoPriorActivity()
            .when(new CreateUserForNewlyRegisteredOrganizationCommand(USER_ID, ORGANIZATION_ID, EMAIL_ADDRESS, USER_DISPLAY_NAME))
            .expectEvents(new UserCreatedForNewlyRegisteredOrganizationEvent(USER_ID, ORGANIZATION_ID, USER_DISPLAY_NAME, EMAIL_ADDRESS));
    }

    @Test
    void createOrganizationUserCommandEmits() {
        testFixture.givenNoPriorActivity()
            .when(new CreateOrganizationUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, EMAIL_ADDRESS, USER_DISPLAY_NAME))
            .expectEvents(new UserCreatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID, USER_DISPLAY_NAME, EMAIL_ADDRESS));
    }

    @Test
    void updateUserDetailsCommandEmits_WhenCommandAccepted() {
        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(new UpdateUserDetailsCommand(USER_ID, EMAIL_ADDRESS_CHANGE,
                DISPLAY_NAME_CHANGE, ADMIN_ID))
            .expectEvents(new UserDetailsUpdatedByAdminEvent(USER_ID, ORGANIZATION_ID, DISPLAY_NAME_CHANGE,
                EMAIL_ADDRESS_CHANGE, ADMIN_ID));
    }

    @Test
    void rejectsAddUserRolesCommand_WhenNotRolesBeingAdded() {
        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(new AddUserRolesCommand(USER_ID, Set.of(), ADMIN_ID))
            .expectNoEvents()
            .expectException(RuntimeException.class)
            .expectExceptionMessage("USER_UPDATE_NO_ROLES_SPECIFIED");
    }

    @Test
    void rejectsAddUserRolesCommand_WhenUserStatusValidatorFails() {
        var command = new AddUserRolesCommand(USER_ID, Set.of(), ADMIN_ID);
        doThrow(IllegalStateException.class).when(userStatusValidator).validate(command);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(IllegalStateException.class);
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenEmailValidatorFails() {
        var command = new UpdateUserDetailsCommand(USER_ID, "not-a-valid-email", DISPLAY_NAME_CHANGE, ADMIN_ID);
        doThrow(IllegalArgumentException.class).when(emailAddressValidator).validate(command);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(IllegalArgumentException.class);
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenRequestingUserIdIsNull() {
        var command = new UpdateUserDetailsCommand(USER_ID, EMAIL_ADDRESS_CHANGE, DISPLAY_NAME_CHANGE, null);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(ConstraintViolationException.class)
            .expectExceptionMessage("requestingUserId: must not be null");
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenNoFieldsAreBeingChanged() {
        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(new UpdateUserDetailsCommand(USER_ID, NO_CHANGE, NO_CHANGE, ADMIN_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("USER_UPDATE_NO_FIELDS_CHANGED");
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenDisplayNameIsBlanked() {
        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(new UpdateUserDetailsCommand(USER_ID, NO_CHANGE, BLANK_FIELD, ADMIN_ID))
            .expectNoEvents()
            .expectException(CommandExecutionException.class)
            .expectExceptionMessage("USER_DISPLAY_NAME_MISSING");
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenUniqueEmailValidatorFails() {
        var command = new UpdateUserDetailsCommand(USER_ID, EMAIL_ADDRESS_CHANGE, DISPLAY_NAME_CHANGE, ADMIN_ID);
        doThrow(IllegalStateException.class).when(usersUniqueEmailValidator).validate(command);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(IllegalStateException.class);
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenUserStatusValidatorFails() {
        var command = new UpdateUserDetailsCommand(USER_ID, EMAIL_ADDRESS_CHANGE, DISPLAY_NAME_CHANGE, ADMIN_ID);
        doThrow(IllegalStateException.class).when(userStatusValidator).validate(command);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(IllegalStateException.class);
    }

    @Test
    void emitsUserProfilePhotoUploadedEvent_WhenProfilePhotoIsUploaded() {
        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(new RegisterUploadedUserProfilePhotoCommand(USER_ID, PROFILE_PHOTO_FILE_ID))
            .expectEvents(new UserProfilePhotoUploadedEvent(USER_ID, PROFILE_PHOTO_FILE_ID));
    }

    @Test
    void rejectsRegisterUploadedUserProfilePhotoCommand_WhenUserStatusValidatorFails() {
        var command = new RegisterUploadedUserProfilePhotoCommand(USER_ID, PROFILE_PHOTO_FILE_ID);
        doThrow(IllegalStateException.class).when(userStatusValidator).validate(command);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(IllegalStateException.class);
    }

    @Test
    void emitsUserDeletedAndForgottenEvent_WhenUserIsDeletedAndForgotten() {
        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, "It's the right thing to do"))
            .expectEvents(USER_DELETED_AND_FORGOTTEN_EVENT);
    }

    @Test
    void rejectsDeleteAndForgetUserCommand_WhenUserHasAlreadyBeenDeleted() {
        var expectedExceptionMessage = String.format("Aggregate with identifier [%s] not found. It has been deleted.", USER_ID);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT, USER_DELETED_AND_FORGOTTEN_EVENT)
            .when(new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, "It's the right thing to do"))
            .expectNoEvents()
            .expectException(AggregateDeletedException.class)
            .expectExceptionMessage(expectedExceptionMessage);
    }

    @Test
    void rejectsDeleteAndForgetUserCommand_WhenUserStatusValidatorFails() {
        var command = new DeleteAndForgetUserCommand(USER_ID, ADMIN_ID, "It's the right thing to do");
        doThrow(IllegalStateException.class).when(userStatusValidator).validate(command);

        testFixture.given(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
            .when(command)
            .expectNoEvents()
            .expectException(IllegalStateException.class);
    }
}
