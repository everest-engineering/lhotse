package engineering.everest.lhotse.registrations.domain;

import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.registrations.domain.commands.CancelConfirmedRegistrationUserEmailAlreadyInUseCommand;
import engineering.everest.lhotse.registrations.domain.commands.CompleteOrganizationRegistrationCommand;
import engineering.everest.lhotse.registrations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCompletedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedAfterUserWithEmailCreatedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationRegistrationSagaTest {

    private static final UUID CONFIRMATION_CODE = randomUUID();
    private static final String CONFIRMATION_CODE_STRING = CONFIRMATION_CODE.toString();
    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID REGISTERING_USER_ID = randomUUID();
    private static final String REGISTERING_USER_EMAIL = "contact@example.com";
    private static final String ORGANIZATION_NAME = "org-name";
    private static final String ORGANIZATION_WEBSITE_URL = "websiteurl";
    private static final String ORGANIZATION_STREET = "street";
    private static final String ORGANIZATION_CITY = "city";
    private static final String ORGANIZATION_STATE = "state";
    private static final String ORGANIZATION_COUNTRY = "country";
    private static final String ORGANIZATION_POST_CODE = "post code";
    private static final String CONTACT_NAME = "Major Tom";
    private static final String CONTACT_PHONE_NUMBER = "555-12345";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final UserCreatedForNewlyRegisteredOrganizationEvent USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT = new UserCreatedForNewlyRegisteredOrganizationEvent(REGISTERING_USER_ID, ORGANIZATION_ID, CONFIRMATION_CODE, CONTACT_NAME, REGISTERING_USER_EMAIL, ENCODED_PASSWORD);
    private static final OrganizationRegistrationReceivedEvent ORGANIZATION_REGISTRATION_RECEIVED_EVENT =
            new OrganizationRegistrationReceivedEvent(ORGANIZATION_ID, REGISTERING_USER_ID, CONFIRMATION_CODE, REGISTERING_USER_EMAIL, ENCODED_PASSWORD, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                    ORGANIZATION_COUNTRY, ORGANIZATION_POST_CODE, CONTACT_NAME, CONTACT_PHONE_NUMBER);
    private static final OrganizationRegistrationConfirmedEvent ORGANIZATION_REGISTRATION_CONFIRMED_EVENT = new OrganizationRegistrationConfirmedEvent(CONFIRMATION_CODE, ORGANIZATION_ID);

    private SagaTestFixture<OrganizationRegistrationSaga> testFixture;

    @Mock
    private UsersReadService usersReadService;

    @BeforeEach
    void setUp() {
        testFixture = new SagaTestFixture<>(OrganizationRegistrationSaga.class);
        testFixture.registerResource(usersReadService);
    }

    @Test
    void organizationRegistrationReceivedEvent_WillSendEmailToConfirmRegistration_AndDispatchRecordSentOrganizationRegistrationEmailConfirmationCommand() {
        var expectedCommand = new RecordSentOrganizationRegistrationEmailConfirmationCommand(CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_EMAIL, ORGANIZATION_NAME, REGISTERING_USER_ID);
        testFixture.givenNoPriorActivity()
                .whenAggregate(CONFIRMATION_CODE_STRING).publishes(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .expectDispatchedCommands(expectedCommand)
                .expectActiveSagas(1);
    }

    @Test
    void organizationRegistrationConfirmedEvent_WillDispatchCommandsToCreateOrganizationAndUser() {
        var expectedCreateOrganizationCommand = new CreateRegisteredOrganizationCommand(ORGANIZATION_ID, REGISTERING_USER_ID, ORGANIZATION_NAME, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POST_CODE, ORGANIZATION_WEBSITE_URL, CONTACT_NAME, CONTACT_PHONE_NUMBER, REGISTERING_USER_EMAIL);
        var expectedCreateUserCommand = new CreateUserForNewlyRegisteredOrganizationCommand(REGISTERING_USER_ID, ORGANIZATION_ID, CONFIRMATION_CODE, REGISTERING_USER_EMAIL, ENCODED_PASSWORD, CONTACT_NAME);
        var expectedPromoteUserCommand = new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID);
        var expectedCompleteRegistrationCommand = new CompleteOrganizationRegistrationCommand(CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID);

        testFixture.givenAggregate(CONFIRMATION_CODE_STRING).published(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .whenAggregate(CONFIRMATION_CODE_STRING).publishes(ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .expectDispatchedCommands(expectedCreateOrganizationCommand, expectedCreateUserCommand, expectedPromoteUserCommand, expectedCompleteRegistrationCommand)
                .expectActiveSagas(1);
    }

    @Test
    void organizationRegistrationConfirmedEvent_WillDispatchCancelConfirmedRegistrationUserEmailAlreadyInUseCommand_WhenUsersEmailIsAlreadyInUse() {
        when(usersReadService.hasUserWithEmail(REGISTERING_USER_EMAIL)).thenReturn(true);

        var expectedCommand = new CancelConfirmedRegistrationUserEmailAlreadyInUseCommand(CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID, REGISTERING_USER_EMAIL);

        testFixture.givenAggregate(CONFIRMATION_CODE_STRING).published(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .whenAggregate(CONFIRMATION_CODE_STRING).publishes(ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .expectDispatchedCommands(expectedCommand)
                .expectActiveSagas(1);
    }

    @Test
    void organizationRegistrationConfirmedAfterUserWithEmailCreatedEvent_WillEndSaga() {
        testFixture.givenAggregate(CONFIRMATION_CODE_STRING).published(
                ORGANIZATION_REGISTRATION_RECEIVED_EVENT,
                ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .whenAggregate(CONFIRMATION_CODE_STRING).publishes(new OrganizationRegistrationConfirmedAfterUserWithEmailCreatedEvent(CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID, REGISTERING_USER_EMAIL))
                .expectNoDispatchedCommands()
                .expectActiveSagas(0);
    }

    @Test
    void organizationRegistrationCompletedEvent_WillEndSaga() {
        testFixture.givenAggregate(CONFIRMATION_CODE_STRING).published(
                ORGANIZATION_REGISTRATION_RECEIVED_EVENT,
                ORGANIZATION_REGISTRATION_CONFIRMED_EVENT,
                USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
                .whenAggregate(CONFIRMATION_CODE_STRING).publishes(new OrganizationRegistrationCompletedEvent(CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID))
                .expectNoDispatchedCommands()
                .expectActiveSagas(0);
    }
}
