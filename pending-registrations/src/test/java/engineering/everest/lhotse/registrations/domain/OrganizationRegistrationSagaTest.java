package engineering.everest.lhotse.registrations.domain;

import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.registrations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static java.util.UUID.randomUUID;

@ExtendWith(MockitoExtension.class)
class OrganizationRegistrationSagaTest {

    private static final UUID CONFIRMATION_CODE = randomUUID();
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
    public static final UserCreatedForNewlyRegisteredOrganizationEvent USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT = new UserCreatedForNewlyRegisteredOrganizationEvent(REGISTERING_USER_ID, ORGANIZATION_ID, CONFIRMATION_CODE, CONTACT_NAME, REGISTERING_USER_EMAIL, ENCODED_PASSWORD);
    private static final OrganizationRegistrationReceivedEvent ORGANIZATION_REGISTRATION_RECEIVED_EVENT =
            new OrganizationRegistrationReceivedEvent(ORGANIZATION_ID, REGISTERING_USER_ID, CONFIRMATION_CODE, REGISTERING_USER_EMAIL, ENCODED_PASSWORD, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                    ORGANIZATION_COUNTRY, ORGANIZATION_POST_CODE, CONTACT_NAME, CONTACT_PHONE_NUMBER);
    private static final OrganizationRegistrationConfirmedEvent ORGANIZATION_REGISTRATION_CONFIRMED_EVENT = new OrganizationRegistrationConfirmedEvent(CONFIRMATION_CODE, ORGANIZATION_ID);

    private SagaTestFixture<OrganizationRegistrationSaga> testFixture;

    @BeforeEach
    void setUp() {
        testFixture = new SagaTestFixture<>(OrganizationRegistrationSaga.class);
    }

    @Test
    void organizationRegistrationReceivedEvent_WillSendEmailToConfirmRegistration_AndDispatchRecordSentOrganizationRegistrationEmailConfirmationCommand() {
        var expectedCommand = new RecordSentOrganizationRegistrationEmailConfirmationCommand(CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_EMAIL, ORGANIZATION_NAME);
        testFixture.givenNoPriorActivity()
                .whenAggregate(CONFIRMATION_CODE.toString()).publishes(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .expectDispatchedCommands(expectedCommand);
    }

    @Test
    void organizationRegistrationConfirmedEvent_WillDispatchCommandsToCreateOrganizationAndUser() {
        var expectedCreateOrganizationCommand = new CreateRegisteredOrganizationCommand(ORGANIZATION_ID, REGISTERING_USER_ID, ORGANIZATION_NAME, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POST_CODE, ORGANIZATION_WEBSITE_URL, CONTACT_NAME, CONTACT_PHONE_NUMBER, REGISTERING_USER_EMAIL);
        var expectedCreateUserCommand = new CreateUserForNewlyRegisteredOrganizationCommand(REGISTERING_USER_ID, ORGANIZATION_ID, CONFIRMATION_CODE, REGISTERING_USER_EMAIL, ENCODED_PASSWORD, CONTACT_NAME);

        testFixture.givenAggregate(CONFIRMATION_CODE.toString()).published(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .whenAggregate(CONFIRMATION_CODE.toString()).publishes(ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .expectDispatchedCommands(expectedCreateOrganizationCommand, expectedCreateUserCommand);
    }

    @Test
    void userCreatedForNewlyRegisteredOrganizationEvent_WillDispatchCommandToPromoteUserToOrganizationAdmin() {
        var expectedCommand = new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID);

        testFixture.givenAggregate(CONFIRMATION_CODE.toString()).published(ORGANIZATION_REGISTRATION_RECEIVED_EVENT, ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .whenAggregate(CONFIRMATION_CODE.toString()).publishes(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
                .expectDispatchedCommands(expectedCommand);
    }
}