package engineering.everest.lhotse.registrations.domain;

import engineering.everest.lhotse.axon.command.validators.EmailAddressValidator;
import engineering.everest.lhotse.i18n.exceptions.TranslatableIllegalArgumentException;
import engineering.everest.lhotse.registrations.domain.commands.CancelConfirmedRegistrationUserEmailAlreadyInUseCommand;
import engineering.everest.lhotse.registrations.domain.commands.CompleteOrganizationRegistrationCommand;
import engineering.everest.lhotse.registrations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.registrations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.registrations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCompletedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmationEmailSentEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCancelledUserWithEmailAddressAlreadyInUseEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import org.axonframework.spring.stereotype.Aggregate;
import org.axonframework.test.aggregate.AggregateTestFixture;
import org.axonframework.test.aggregate.FixtureConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static engineering.everest.lhotse.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class PendingRegistrationAggregateTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID REGISTERING_USER_ID = randomUUID();
    private static final UUID REGISTRATION_CONFIRMATION_CODE = randomUUID();
    public static final OrganizationRegistrationConfirmedEvent ORGANIZATION_REGISTRATION_CONFIRMED_EVENT = new OrganizationRegistrationConfirmedEvent(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID);
    private static final String ORGANIZATION_NAME = "organization-name";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String ORGANIZATION_STREET = "street";
    private static final String ORGANIZATION_CITY = "city";
    private static final String ORGANIZATION_STATE = "state";
    private static final String ORGANIZATION_COUNTRY = "country";
    private static final String ORGANIZATION_POSTAL_CODE = "postal";
    private static final String ORGANIZATION_WEBSITE_URL = "website-url";
    private static final String ORGANIZATION_CONTACT_NAME = "contact-name";
    private static final String ORGANIZATION_CONTACT_PHONE_NUMBER = "phone-number";
    private static final String ORGANIZATION_CONTACT_EMAIL_ADDRESS = "email@domain.com";

    private static final OrganizationRegistrationReceivedEvent ORGANIZATION_REGISTRATION_RECEIVED_EVENT =
            new OrganizationRegistrationReceivedEvent(ORGANIZATION_ID, REGISTERING_USER_ID, REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_CONTACT_EMAIL_ADDRESS,
                    ENCODED_PASSWORD, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY,
                    ORGANIZATION_POSTAL_CODE, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER);

    private FixtureConfiguration<PendingRegistrationAggregate> testFixture;

    @Mock
    private EmailAddressValidator emailAddressValidator;

    @BeforeEach
    void setUp() {
        testFixture = new AggregateTestFixture<>(PendingRegistrationAggregate.class)
                .registerCommandHandlerInterceptor(
                        mockCommandValidatingMessageHandlerInterceptor(emailAddressValidator));
    }

    @Test
    void aggregateHasExplicitlyDefinedRepository() {
        var organizationClass = PendingRegistrationAggregate.class;
        var aggregateAnnotation = organizationClass.getAnnotation(Aggregate.class);
        assertEquals(aggregateAnnotation.repository(), "repositoryForPendingRegistration");
    }

    @Test
    void emits_WhenRegisterOrganizationCommandIsValid() {
        testFixture.givenNoPriorActivity()
                .when(new RegisterOrganizationCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID, ORGANIZATION_CONTACT_EMAIL_ADDRESS,
                        ENCODED_PASSWORD, ORGANIZATION_NAME, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY,
                        ORGANIZATION_POSTAL_CODE, ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_CONTACT_PHONE_NUMBER))
                .expectEvents(ORGANIZATION_REGISTRATION_RECEIVED_EVENT);
    }

    @Test
    void emits_WhenRecordSentOrganizationRegistrationEmailConfirmationCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new RecordSentOrganizationRegistrationEmailConfirmationCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID, ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_NAME, REGISTERING_USER_ID))
                .expectEvents(new OrganizationRegistrationConfirmationEmailSentEvent(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID, ORGANIZATION_CONTACT_EMAIL_ADDRESS, ORGANIZATION_NAME, REGISTERING_USER_ID));
    }

    @Test
    void emits_WhenConfirmOrganizationRegistrationEmailCommandIsValid() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new ConfirmOrganizationRegistrationEmailCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID))
                .expectEvents(ORGANIZATION_REGISTRATION_CONFIRMED_EVENT);
    }

    @Test
    void rejectsConfirmOrganizationRegistrationEmailCommand_WhenOrganizationIdIncorrect() {
        testFixture.given(ORGANIZATION_REGISTRATION_RECEIVED_EVENT)
                .when(new ConfirmOrganizationRegistrationEmailCommand(REGISTRATION_CONFIRMATION_CODE, randomUUID()))
                .expectNoEvents()
                .expectException(TranslatableIllegalArgumentException.class)
                .expectExceptionMessage("ORGANIZATION_REGISTRATION_TOKEN_FOR_ANOTHER_ORG");
    }

    @Test
    void emits_WhenCompleteOrganizationRegistrationCommandIsValid() {
        testFixture.given(
                ORGANIZATION_REGISTRATION_RECEIVED_EVENT,
                ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .when(new CompleteOrganizationRegistrationCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID))
                .expectEvents(new OrganizationRegistrationCompletedEvent(ORGANIZATION_ID, REGISTERING_USER_ID));
    }

    @Test
    void emits_WhenCancelConfirmedRegistrationUserEmailAlreadyInUseCommandIsValid() {
        testFixture.given(
                ORGANIZATION_REGISTRATION_RECEIVED_EVENT,
                ORGANIZATION_REGISTRATION_CONFIRMED_EVENT)
                .when(new CancelConfirmedRegistrationUserEmailAlreadyInUseCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID, REGISTERING_USER_ID, ORGANIZATION_CONTACT_EMAIL_ADDRESS))
                .expectEvents(new OrganizationRegistrationCancelledUserWithEmailAddressAlreadyInUseEvent(ORGANIZATION_ID, REGISTERING_USER_ID, ORGANIZATION_CONTACT_EMAIL_ADDRESS));
    }
}
