package engineering.everest.lhotse.registrations.eventhandlers;

import engineering.everest.lhotse.registrations.persistence.PendingRegistrationsRepository;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCompletedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCancelledUserWithEmailAddressAlreadyInUseEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PendingRegistrationsEventHandlerTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID REGISTERING_USER_ID = randomUUID();
    private static final UUID REGISTRATION_CONFIRMATION_CODE = randomUUID();
    private static final String CONTACT_EMAIL = "email@example.com";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String ORGANIZATION_NAME = "organization-name";
    private static final String WEBSITE_URL = "websiteurl";
    private static final String STREET = "street";
    private static final String CITY = "city";
    private static final String STATE = "state";
    private static final String COUNTRY = "country";
    private static final String POSTAL_CODE = "postal-code";
    private static final String CONTACT_NAME = "contact name";
    private static final String CONTACT_PHONE_NUMBER = "contact phone number";
    private static final Instant REGISTRATION_RECEIVED_TIME = Instant.now();

    private PendingRegistrationsEventHandler pendingRegistrationsEventHandler;

    @Mock
    private PendingRegistrationsRepository pendingRegistrationsRepository;

    @BeforeEach
    void setUp() {
        pendingRegistrationsEventHandler = new PendingRegistrationsEventHandler(pendingRegistrationsRepository);
    }

    @Test
    void prepareForReplay_WillDropProjections() {
        pendingRegistrationsEventHandler.prepareForReplay();

        verify(pendingRegistrationsRepository).deleteAll();
    }

    @Test
    void onOrganizationRegistrationReceived_WillDelegateToRepository() {
        var organizationRegistrationReceivedEvent = new OrganizationRegistrationReceivedEvent(ORGANIZATION_ID, REGISTERING_USER_ID, REGISTRATION_CONFIRMATION_CODE, CONTACT_EMAIL, ENCODED_PASSWORD, ORGANIZATION_NAME, WEBSITE_URL, STREET, CITY, STATE, COUNTRY, POSTAL_CODE, CONTACT_NAME, CONTACT_PHONE_NUMBER);
        pendingRegistrationsEventHandler.on(organizationRegistrationReceivedEvent, REGISTRATION_RECEIVED_TIME);

        verify(pendingRegistrationsRepository).createPendingRegistration(ORGANIZATION_ID, REGISTRATION_CONFIRMATION_CODE, REGISTERING_USER_ID, CONTACT_EMAIL, REGISTRATION_RECEIVED_TIME);
    }

    @Test
    void onOrganizationRegistrationCompleted_WillDeleteProjectedPendingRegistration() {
        var organizationRegistrationCompletedEvent = new OrganizationRegistrationCompletedEvent(ORGANIZATION_ID, REGISTERING_USER_ID);
        pendingRegistrationsEventHandler.on(organizationRegistrationCompletedEvent);

        verify(pendingRegistrationsRepository).deleteById(ORGANIZATION_ID);
    }

    @Test
    void onOrganizationRegistrationConfirmedAfterUserWithEmailCreated_WillDeleteProjectedPendingRegistration() {
        var organizationRegistrationConfirmedAfterUserWithEmailCreatedEvent = new OrganizationRegistrationCancelledUserWithEmailAddressAlreadyInUseEvent(ORGANIZATION_ID, REGISTERING_USER_ID, CONTACT_EMAIL);
        pendingRegistrationsEventHandler.on(organizationRegistrationConfirmedAfterUserWithEmailCreatedEvent);

        verify(pendingRegistrationsRepository).deleteById(ORGANIZATION_ID);
    }
}
