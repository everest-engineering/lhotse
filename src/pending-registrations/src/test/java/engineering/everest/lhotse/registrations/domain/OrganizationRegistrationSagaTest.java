package engineering.everest.lhotse.registrations.domain;

import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredEvent;
import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
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
    private static final UserCreatedForNewlyRegisteredOrganizationEvent USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT = new UserCreatedForNewlyRegisteredOrganizationEvent(REGISTERING_USER_ID, ORGANIZATION_ID, CONTACT_NAME, REGISTERING_USER_EMAIL, ENCODED_PASSWORD);
    private static final OrganizationRegisteredEvent ORGANIZATION_REGISTERED_EVENT = new OrganizationRegisteredEvent(ORGANIZATION_ID, REGISTERING_USER_ID, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POST_CODE, CONTACT_NAME, CONTACT_PHONE_NUMBER, REGISTERING_USER_EMAIL);
    private static final UserPromotedToOrganizationAdminEvent USER_PROMOTED_TO_ORGANIZATION_ADMIN_EVENT = new UserPromotedToOrganizationAdminEvent(ORGANIZATION_ID, REGISTERING_USER_ID);

    private SagaTestFixture<OrganizationRegistrationSaga> testFixture;

    @Mock
    private UsersReadService usersReadService;
    @Mock
    private OrganizationsReadService organizationsReadService;

    @BeforeEach
    void setUp() {
        testFixture = new SagaTestFixture<>(OrganizationRegistrationSaga.class);
        testFixture.registerResource(usersReadService);
        testFixture.registerResource(organizationsReadService);
    }

    @Test
    void organisationRegisteredEvent_WillDispatchCommandToCreateUserForNewlyRegisteredOrganization() {
        var expectedCreateUserCommand = new CreateUserForNewlyRegisteredOrganizationCommand(REGISTERING_USER_ID, ORGANIZATION_ID, REGISTERING_USER_EMAIL, ENCODED_PASSWORD, CONTACT_NAME);
        testFixture.givenAggregate(CONFIRMATION_CODE.toString()).published()
                .whenAggregate(ORGANIZATION_ID.toString()).publishes(ORGANIZATION_REGISTERED_EVENT)
                .expectDispatchedCommands(expectedCreateUserCommand)
                .expectActiveSagas(1);
    }

    @Test
    void userCreatedForNewlyRegisteredOrganizationEvent_WillDispatchCommandToPromoteUserToOrgAdmin() {
        when(usersReadService.exists(REGISTERING_USER_ID)).thenReturn(true);
        when(organizationsReadService.exists(ORGANIZATION_ID)).thenReturn(true);

        var expectedPromoteUserCommand = new PromoteUserToOrganizationAdminCommand(ORGANIZATION_ID, REGISTERING_USER_ID);
        testFixture.givenAggregate(CONFIRMATION_CODE.toString()).published(
                        ORGANIZATION_REGISTERED_EVENT)
                .whenAggregate(REGISTERING_USER_ID.toString()).publishes(USER_CREATED_FOR_NEWLY_REGISTERED_ORGANIZATION_EVENT)
                .expectDispatchedCommands(expectedPromoteUserCommand)
                .expectActiveSagas(1);
    }

    @Test
    void userPromotedToOrganizationAdminEvent_WillDispatchCommandToCompleteSaga() {
        when(usersReadService.exists(REGISTERING_USER_ID)).thenReturn(true);
        when(organizationsReadService.exists(ORGANIZATION_ID)).thenReturn(true);

        // TODO
    }
    
}
