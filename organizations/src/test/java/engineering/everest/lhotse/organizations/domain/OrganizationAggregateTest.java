package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import engineering.everest.lhotse.axon.command.validators.UsersBelongToOrganizationValidator;
import engineering.everest.lhotse.organizations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.DisableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.EnableOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.organizations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationDisabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationEnabledByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationNameUpdatedByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredByAdminEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationConfirmationEmailSentEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.starterkit.filestorage.FileService;
import engineering.everest.starterkit.filestorage.InputStreamOfKnownLength;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.validation.ConstraintViolationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrganizationAggregateTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID REGISTERING_USER_ID = randomUUID();
    private static final UUID REGISTRATION_CONFIRMATION_CODE = randomUUID();
    private static final String ORGANIZATION_NAME = "organization-name";
    private static final String NO_CHANGE = null;
    private static final String MISSING_ARGUMENT = null;
    private static final String ORGANIZATION_STREET = "street";
    private static final String ORGANIZATION_CITY = "city";
    private static final String ORGANIZATION_STATE = "state";
    private static final String ORGANIZATION_COUNTRY = "country";
    private static final String ORGANIZATION_POSTAL_CODE = "postal";
    private static final String ORGANIZATION_WEBSITE_URL = "website-url";
    private static final String ORGANIZATION_CONTACT_NAME = "contact-name";
    private static final String ORGANIZATION_CONTACT_PHONE_NUMBER = "phone-number";
    private static final String ORGANIZATION_CONTACT_EMAIL_ADDRESS = "email@domain.com";
    private static final String ORGANIZATION_CONTACT_RAW_PASSWORD = "raw-password";
    private static final OrganizationRegisteredByAdminEvent ORGANIZATION_REGISTERED_BY_ADMIN_EVENT =
            new OrganizationRegisteredByAdminEvent(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL,
                    ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                    ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);
    private static final OrganizationRegistrationReceivedEvent ORGANIZATION_REGISTRATION_RECEIVED_EVENT =
            new OrganizationRegistrationReceivedEvent(ORGANIZATION_ID, REGISTERING_USER_ID, REGISTRATION_CONFIRMATION_CODE,
                    ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_CONTACT_RAW_PASSWORD, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL,
                    ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                    ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER);

    private static final OrganizationDisabledByAdminEvent ORGANIZATION_DISABLED_BY_ADMIN_EVENT = new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID);

    private static final UUID NETWORK_FILE_ID = randomUUID();
    private static final String FILE_CONTENT = "file-content";

    private FixtureConfiguration<OrganizationAggregate> testFixture;

    @Mock
    private EmailAddressValidator emailAddressValidator;
    @Mock
    private FileService fileService;
    @Mock
    private UsersBelongToOrganizationValidator usersBelongToOrganizationValidator;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(fileService.stream(NETWORK_FILE_ID)).thenReturn(new InputStreamOfKnownLength(
                new ByteArrayInputStream(FILE_CONTENT.getBytes()), FILE_CONTENT.length()));

        testFixture = new AggregateTestFixture<>(OrganizationAggregate.class)
                .registerCommandHandlerInterceptor(mockCommandValidatingMessageHandlerInterceptor(
                        emailAddressValidator, usersBelongToOrganizationValidator))
                .registerInjectableResource(fileService);
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var organizationClass = OrganizationAggregate.class;
        var aggregateAnnotation = organizationClass.getAnnotation(Aggregate.class);
        assertEquals(aggregateAnnotation.repository(), "repositoryForOrganization");
    }

    @Test
    void emits_WhenCreateRegisteredOrganizationCommandIsValid() {
        testFixture.givenNoPriorActivity()
                .when(new CreateRegisteredOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, ORGANIZATION_STREET,
                        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                        ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
                .expectEvents(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT);
    }

    @Test
    void organizationsCreatedByAdminAreEnabled() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is already enabled");
    }

    @Test
    void rejectsCreateRegisteredOrganizationCommand_WhenRequestingUserIdIsNull() {
        var command = new CreateRegisteredOrganizationCommand(ORGANIZATION_ID, null, ORGANIZATION_NAME, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsCreateRegisteredOrganizationCommand_WhenOrganizationNameIsBlank() {
        testFixture.givenNoPriorActivity()
                .when(new CreateRegisteredOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, ORGANIZATION_STREET,
                        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                        ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
                .expectException(ConstraintViolationException.class)
                .expectNoEvents();
    }

    @Test
    void rejectsCreateRegisteredOrganizationCommand_WhenOrganizationNameIsNull() {
        testFixture.givenNoPriorActivity()
                .when(new CreateRegisteredOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, MISSING_ARGUMENT, ORGANIZATION_STREET,
                        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                        ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
                .expectException(ConstraintViolationException.class)
                .expectNoEvents();
    }

    @Test
    void emits_WhenRegisterOrganizationCommandIsValid() {
        testFixture.givenNoPriorActivity()
                .when(new RegisterOrganizationCommand(ORGANIZATION_ID, REGISTERING_USER_ID, REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_CONTACT_EMAIL_ADDRESS,
                        ORGANIZATION_CONTACT_RAW_PASSWORD, ORGANIZATION_NAME, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY,
                        ORGANIZATION_POSTAL_CODE, ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER))
                .expectEvents(ORGANIZATION_REGISTRATION_RECEIVED_EVENT);
    }

    @Test
    void organizationsRegisteredByUsersAreDisabled() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new DisableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is already disabled");
    }

    @Test
    void emits_WhenRecordSentOrganizationRegistrationEmailConfirmationCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new RecordSentOrganizationRegistrationEmailConfirmationCommand(ORGANIZATION_ID, REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_NAME))
                .expectEvents(new OrganizationRegistrationConfirmationEmailSentEvent(ORGANIZATION_ID, REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_NAME));
    }

    @Test
    void emits_WhenConfirmOrganizationRegistrationEmailCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new ConfirmOrganizationRegistrationEmailCommand(ORGANIZATION_ID, REGISTRATION_CONFIRMATION_CODE))
                .expectEvents(new OrganizationRegistrationConfirmedEvent(ORGANIZATION_ID));
    }

    @Test
    void rejectsConfirmOrganizationRegistrationEmailCommand_WhenConfirmationCodeIsIncorrect() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new ConfirmOrganizationRegistrationEmailCommand(ORGANIZATION_ID, randomUUID()))
                .expectNoEvents()
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("Organization registration confirmation code did not match");
    }

    @Test
    void emits_WhenDisableOrganizationCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(new DisableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectEvents(new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));
    }

    @Test
    void emits_WhenEnableOrganizationCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT,
                new OrganizationDisabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID))
                .when(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectEvents(new OrganizationEnabledByAdminEvent(ORGANIZATION_ID, ADMIN_ID));
    }

    @Test
    void rejectsEnableOrganizationCommand_WhenOrganizationIsAlreadyEnabled() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(new EnableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is already enabled");
    }

    @Test
    void rejectsDisableOrganizationCommand_WhenOrganizationIsAlreadyDisabled() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT, ORGANIZATION_DISABLED_BY_ADMIN_EVENT)
                .when(new DisableOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is already disabled");
    }

    @Test
    void rejectsDisableOrganizationCommand_WhenRequestingUserIdIsNull() {
        var command = new DisableOrganizationCommand(ORGANIZATION_ID, null);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class)
                .expectExceptionMessage("requestingUserId: must not be null");
    }

    @Test
    void updateOrganizationCommandEmitsSingleEvent_WhenOnlyOrganizationNameChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, "new org name",
                NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectEvents(new OrganizationNameUpdatedByAdminEvent(ORGANIZATION_ID, "new org name", ADMIN_ID));
    }

    @Test
    void updateOrganizationCommandEmitsSingleEvent_WhenOnlyOrganizationContactDetailsChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, NO_CHANGE, NO_CHANGE,
                NO_CHANGE, NO_CHANGE, NO_CHANGE, ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER,
                ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectEvents(
                        new OrganizationContactDetailsUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_WEBSITE_URL, ADMIN_ID));
    }

    @Test
    void updateOrganizationCommandEmitsSingleEvent_WhenOnlyOrganizationAddressChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, ORGANIZATION_STREET, ORGANIZATION_CITY,
                ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectEvents(
                        new OrganizationAddressUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ADMIN_ID));
    }

    @Test
    void updateOrganizationCommandEmitsMultipleEvents_WhenOrganiztionNameChangedAndContactDetailsChangedAndAddressChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, "new org name",
                ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectEvents(
                        new OrganizationNameUpdatedByAdminEvent(ORGANIZATION_ID, "new org name", ADMIN_ID),
                        new OrganizationContactDetailsUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_WEBSITE_URL, ADMIN_ID),
                        new OrganizationAddressUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ADMIN_ID));
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenRequestingUserIdIsNull() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, null, ORGANIZATION_NAME,
                ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class)
                .expectExceptionMessage("requestingUserId: must not be null");
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenOrganizationIsAlreadyDisabled() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME,
                ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER, ORGANIZATION_CONTACT_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT, ORGANIZATION_DISABLED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is already disabled");
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenNoFieldsAreBeingChanged() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, NO_CHANGE,
                NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE, NO_CHANGE);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(IllegalArgumentException.class)
                .expectExceptionMessage("At least one organization field change must be requested");
    }

    @Test
    void emits_WhenPromoteUserToOrganizationAdminCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT,
                new OrganizationRegistrationConfirmedEvent(ORGANIZATION_ID))
                .when(new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID))
                .expectEvents(new UserPromotedToOrganizationAdminEvent(ORGANIZATION_ID, REGISTERING_USER_ID));
    }

    @Test
    void rejectsPromoteUserToOrganizationAdminCommand_WhenOrganizationIsDisabled() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID))
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is disabled");
    }
}
