package engineering.everest.starterkit.axon.users.domain;

import engineering.everest.starterkit.axon.command.validators.EmailAddressValidator;
import engineering.everest.starterkit.axon.command.validators.OrganizationStatusValidator;
import engineering.everest.starterkit.axon.command.validators.UsersUniqueEmailValidator;
import engineering.everest.starterkit.axon.common.domain.User;
import engineering.everest.starterkit.axon.users.domain.commands.CreateUserCommand;
import engineering.everest.starterkit.axon.users.domain.commands.RegisterUploadedUserProfilePhotoCommand;
import engineering.everest.starterkit.axon.users.domain.commands.UpdateUserDetailsCommand;
import engineering.everest.starterkit.axon.users.domain.events.OrgAdminAssignedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserAccountStatusUpdatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserCreatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.axon.users.domain.events.UserProfilePhotoUploadedEvent;
import engineering.everest.starterkit.axon.users.services.UsersReadService;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.EnumSet;
import java.util.UUID;
import javax.validation.ConstraintViolationException;

import static engineering.everest.starterkit.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static engineering.everest.starterkit.axon.common.domain.Role.ORG_USER;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class UserAggregateTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
    private static final UUID USER_ID = randomUUID();
    private static final UUID USER_ID_2 = randomUUID();
    private static final UUID PROFILE_PHOTO_FILE_ID = randomUUID();
    private static final String USERNAME = "user@email.com";
    private static final String USER_DISPLAY_NAME = "user-display-name";
    private static final String USER_EMAIL = USERNAME;
    private static final String USER_ENCODED_PASSWORD = "encoded-password";
    private static final String DISPLAY_NAME_CHANGE = "display-name-change";
    private static final String ENCODED_PASSWORD_CHANGE = "encoded-password-change";
    private static final String EMAIL_CHANGE = "email@change.com";
    private static final String NO_CHANGE = null;
    private static final String BLANK_FIELD = "";

    private static final User USER = new User(USER_ID, ORGANIZATION_ID, USERNAME, USER_DISPLAY_NAME, USER_EMAIL, false, EnumSet.of(ORG_USER));
    private static final User USER_2 = new User(USER_ID_2, ORGANIZATION_ID, USERNAME, USER_DISPLAY_NAME, USER_EMAIL, false, EnumSet.of(ORG_USER));


    private static final UserCreatedByAdminEvent USER_CREATED_BY_ADMIN_EVENT =
            new UserCreatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID, USER_DISPLAY_NAME, USERNAME, USER_ENCODED_PASSWORD);

    private static final OrgAdminAssignedByAdminEvent ORG_ADMIN_1_ASSIGNED_BY_ADMIN_EVENT = new OrgAdminAssignedByAdminEvent(ORGANIZATION_ID, USER_ID, ADMIN_ID);

    private FixtureConfiguration<UserAggregate> testFixture;

    private static final UserAccountStatusUpdatedByAdminEvent ORG_USER_ACCOUNT_STATUS_UPDATED_BY_ADMIN_EVENT =
            new UserAccountStatusUpdatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID, true);

    @Mock
    private UsersReadService usersReadService;
    @Mock
    private EmailAddressValidator emailAddressValidator;
    @Mock
    private UsersUniqueEmailValidator usersUniqueEmailValidator;
    @Mock
    private OrganizationStatusValidator organizationStatusValidator;

    @BeforeEach
    void setUp() {
        testFixture = new AggregateTestFixture<>(UserAggregate.class)
                .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor(
                        emailAddressValidator,
                        usersUniqueEmailValidator,
                        organizationStatusValidator))
                .registerInjectableResource(usersReadService);
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var organizationClass = UserAggregate.class;
        var aggregateAnnotation = organizationClass.getAnnotation(Aggregate.class);
        assertEquals(aggregateAnnotation.repository(), "repositoryForUser");
    }

    @Test
    void createUserCommandEmits_WhenAllMandatoryFieldsArePresentInCreationCommand() {
        testFixture.givenNoPriorActivity()
                .when(new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, USERNAME, USER_ENCODED_PASSWORD, USER_DISPLAY_NAME))
                .expectEvents(new UserCreatedByAdminEvent(USER_ID, ORGANIZATION_ID, ADMIN_ID, USER_DISPLAY_NAME, USERNAME, USER_ENCODED_PASSWORD));
    }

    @Test
    void rejectsCreateUserCommand_WhenEmailValidatorFails() {
        CreateUserCommand command = new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, null, USER_ENCODED_PASSWORD, USER_DISPLAY_NAME);

        testFixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserCommand_WhenDisplayNameIsBlank() {
        testFixture.givenNoPriorActivity()
                .when(new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, USERNAME, USER_ENCODED_PASSWORD, ""))
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserCommand_WhenDisplayNameIsNull() {
        testFixture.givenNoPriorActivity()
                .when(new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, USERNAME, USER_ENCODED_PASSWORD, null))
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserCommand_WhenRequestingUserIsNull() {
        CreateUserCommand command = new CreateUserCommand(USER_ID, ORGANIZATION_ID, null, USERNAME, USER_ENCODED_PASSWORD, USER_DISPLAY_NAME);

        testFixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateUserCommand_WhenOrganizationIdIsInvalid() {
        var command = new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, USERNAME, USER_ENCODED_PASSWORD, USER_DISPLAY_NAME);
        doThrow(IllegalStateException.class).when(organizationStatusValidator).validate(command);

        testFixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents()
                .expectException(IllegalStateException.class);
    }

    @Test
    void rejectsCreateUserCommand_WhenUniqueUserEmailValidatorFails() {
        CreateUserCommand command = new CreateUserCommand(USER_ID, ORGANIZATION_ID, ADMIN_ID, USERNAME,
                USER_DISPLAY_NAME, USER_ENCODED_PASSWORD);
        doThrow(IllegalArgumentException.class).when(usersUniqueEmailValidator).validate(command);

        testFixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents()
                .expectException(IllegalArgumentException.class);
    }

    @Test
    void updateUserDetailsCommandEmits_WhenCommandAccepted() {
        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(new UpdateUserDetailsCommand(USER_ID, EMAIL_CHANGE,
                        DISPLAY_NAME_CHANGE, ENCODED_PASSWORD_CHANGE, ADMIN_ID))
                .expectEvents(new UserDetailsUpdatedByAdminEvent(USER_ID, ORGANIZATION_ID, DISPLAY_NAME_CHANGE,
                        EMAIL_CHANGE, ENCODED_PASSWORD_CHANGE, ADMIN_ID));
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenEmailValidatorFails() {
        var command = new UpdateUserDetailsCommand(USER_ID, "not-a-valid-email",
                DISPLAY_NAME_CHANGE, ENCODED_PASSWORD_CHANGE, ADMIN_ID);
        doThrow(IllegalArgumentException.class).when(emailAddressValidator).validate(command);

        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(IllegalArgumentException.class);
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenRequestingUserIdIsNull() {
        var command = new UpdateUserDetailsCommand(USER_ID, EMAIL_CHANGE,
                DISPLAY_NAME_CHANGE, ENCODED_PASSWORD_CHANGE, null);

        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenNoFieldsAreBeingChanged() {
        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(new UpdateUserDetailsCommand(USER_ID, NO_CHANGE, NO_CHANGE, NO_CHANGE, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("At least one user field change must be requested");
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenDisplayNameIsBlanked() {
        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(new UpdateUserDetailsCommand(USER_ID, NO_CHANGE, BLANK_FIELD, NO_CHANGE, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("User display name is required");
    }

    @Test
    void rejectsUpdateUserDetailsCommand_WhenUniqueEmailValidatorFails() {
        UpdateUserDetailsCommand command = new UpdateUserDetailsCommand(USER_ID, EMAIL_CHANGE, DISPLAY_NAME_CHANGE, NO_CHANGE, ADMIN_ID);

        doThrow(IllegalStateException.class).when(usersUniqueEmailValidator).validate(command);

        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(IllegalStateException.class);
    }

    @Test
    void emitsUserProfilePhotoUploadedEvent_WhenProfilePhotoIsUploaded() {
        testFixture.given(USER_CREATED_BY_ADMIN_EVENT)
                .when(new RegisterUploadedUserProfilePhotoCommand(USER_ID, PROFILE_PHOTO_FILE_ID))
                .expectEvents(new UserProfilePhotoUploadedEvent(USER_ID, PROFILE_PHOTO_FILE_ID));
    }

}
