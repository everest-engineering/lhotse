package engineering.everest.lhotse.registrations.services;

import engineering.everest.axon.HazelcastCommandGateway;
import engineering.everest.lhotse.axon.common.RandomFieldsGenerator;
import engineering.everest.lhotse.registrations.domain.commands.ConfirmOrganizationRegistrationEmailCommand;
import engineering.everest.lhotse.registrations.domain.commands.RegisterOrganizationCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultPendingRegistrationsServiceTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID ORG_ADMIN_USER_ID = randomUUID();
    private static final UUID REGISTRATION_CONFIRMATION_CODE = randomUUID();
    private static final String ORG_ADMIN_RAW_PASSWORD = "raw-password";
    private static final String ORG_ADMIN_ENCODED_PASSWORD = "encoded-password";
    private static final String ORG_ADMIN_EMAIL_ADDRESS_1 = "EmailAddress";
    private static final String ORGANIZATION_NAME = "organization-name";
    private static final String ORGANIZATION_STREET_1 = "street-1";
    private static final String ORGANIZATION_CITY_1 = "city-1";
    private static final String ORGANIZATION_STATE_1 = "state-1";
    private static final String ORGANIZATION_COUNTRY_1 = "country-1";
    private static final String ORGANIZATION_POSTAL_CODE_1 = "postal-1";
    private static final String ORGANIZATION_WEBSITE_URL_1 = "website-1";
    private static final String ORGANIZATION_CONTACT_NAME_1 = "contactName";
    private static final String ORGANIZATION_PHONE_NUMBER_1 = "phoneNumber";

    @Mock
    private RandomFieldsGenerator randomFieldsGenerator;
    @Mock
    private HazelcastCommandGateway commandGateway;
    @Mock
    private PasswordEncoder passwordEncoder;

    private DefaultPendingRegistrationsService defaultPendingRegistrationsService;

    @BeforeEach
    void setUp() {
        defaultPendingRegistrationsService = new DefaultPendingRegistrationsService(randomFieldsGenerator, commandGateway, passwordEncoder);
    }

    @Test
    void registeredOrganisation_WillSendCommandAndWaitForCompletion() {
        var expectedCommand = new RegisterOrganizationCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID, ORG_ADMIN_USER_ID, ORG_ADMIN_EMAIL_ADDRESS_1, ORG_ADMIN_ENCODED_PASSWORD, ORGANIZATION_NAME,
                ORGANIZATION_STREET_1, ORGANIZATION_CITY_1, ORGANIZATION_STATE_1, ORGANIZATION_COUNTRY_1, ORGANIZATION_POSTAL_CODE_1,
                ORGANIZATION_WEBSITE_URL_1, ORGANIZATION_CONTACT_NAME_1, ORGANIZATION_PHONE_NUMBER_1);

        when(randomFieldsGenerator.genRandomUUID()).thenReturn(REGISTRATION_CONFIRMATION_CODE);
        when(commandGateway.sendAndWait(expectedCommand)).thenReturn(ORGANIZATION_ID);
        when(passwordEncoder.encode(ORG_ADMIN_RAW_PASSWORD)).thenReturn(ORG_ADMIN_ENCODED_PASSWORD);

        defaultPendingRegistrationsService.registerOrganization(ORGANIZATION_ID, ORG_ADMIN_USER_ID, ORGANIZATION_NAME, ORGANIZATION_STREET_1, ORGANIZATION_CITY_1, ORGANIZATION_STATE_1,
                ORGANIZATION_COUNTRY_1, ORGANIZATION_POSTAL_CODE_1, ORGANIZATION_WEBSITE_URL_1, ORGANIZATION_CONTACT_NAME_1, ORGANIZATION_PHONE_NUMBER_1, ORG_ADMIN_EMAIL_ADDRESS_1, ORG_ADMIN_RAW_PASSWORD);

        verify(commandGateway).sendAndWait(expectedCommand);
    }

    @Test
    void confirmOrganizationRegistrationEmail_WillSendCommandAndWaitForCompletion() {
        defaultPendingRegistrationsService.confirmOrganizationRegistrationEmail(ORGANIZATION_ID, REGISTRATION_CONFIRMATION_CODE);

        verify(commandGateway).sendAndWait(new ConfirmOrganizationRegistrationEmailCommand(REGISTRATION_CONFIRMATION_CODE, ORGANIZATION_ID));
    }
}

