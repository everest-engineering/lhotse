package engineering.everest.starterkit.organizations.domain;

import engineering.everest.starterkit.axon.command.validators.EmailAddressValidator;
import engineering.everest.starterkit.axon.command.validators.UsersBelongToOrganizationValidator;
import engineering.everest.starterkit.axon.filehandling.FileService;
import engineering.everest.starterkit.organizations.domain.commands.DeregisterOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.commands.RegisterOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.commands.UpdateOrganizationCommand;
import engineering.everest.starterkit.organizations.domain.events.OrganizationAddressUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationContactDetailsUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationDeregisteredByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationNameUpdatedByAdminEvent;
import engineering.everest.starterkit.organizations.domain.events.OrganizationRegisteredByAdminEvent;
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

import static engineering.everest.starterkit.axon.AxonTestUtils.mockCommandValidatingMessageHandlerInterceptor;
import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class OrganizationAggregateTest {

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID ADMIN_ID = randomUUID();
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
    private static final String ORGANIZATION_PHONE_NUMBER = "phone-number";
    private static final String ORGANIZATION_EMAIL_ADDRESS = "email@domain.com";
    private static final OrganizationRegisteredByAdminEvent ORGANIZATION_REGISTERED_BY_ADMIN_EVENT =
            new OrganizationRegisteredByAdminEvent(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, ORGANIZATION_WEBSITE_URL,
                    ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                    ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS);

    private static final OrganizationDeregisteredByAdminEvent ORGANIZATION_DEREGISTERED_BY_ADMIN_EVENT = new OrganizationDeregisteredByAdminEvent(ORGANIZATION_ID, ADMIN_ID);

    private static final UUID NETWORK_FILE_ID = randomUUID();

    private FixtureConfiguration<OrganizationAggregate> testFixture;

    @Mock
    private EmailAddressValidator emailAddressValidator;
    @Mock
    private FileService fileService;
    @Mock
    private UsersBelongToOrganizationValidator usersBelongToOrganizationValidator;

    @BeforeEach
    void setUp() throws IOException {
        lenient().when(fileService.stream(NETWORK_FILE_ID)).thenReturn(new ByteArrayInputStream("file-content".getBytes()));

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
    void emitsOrganizationRegisteredByAdminEvent() {
        testFixture.givenNoPriorActivity()
                .when(new RegisterOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME, ORGANIZATION_STREET,
                        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                        ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS))
                .expectEvents(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT);
    }

    @Test
    void rejectsRegisterOrganizationCommand_WhenRequestingUserIdIsNull() {
        var command = new RegisterOrganizationCommand(ORGANIZATION_ID, null, ORGANIZATION_NAME, ORGANIZATION_STREET,
                ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS);

        testFixture.givenNoPriorActivity()
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsRegisterOrganizationCommand_WhenOrganizationNameIsBlank() {
        testFixture.givenNoPriorActivity()
                .when(new RegisterOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, NO_CHANGE, ORGANIZATION_STREET,
                        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                        ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS))
                .expectException(ConstraintViolationException.class)
                .expectNoEvents();
    }

    @Test
    void rejectsRegisterOrganizationCommand_WhenOrganizationNameIsNull() {
        testFixture.givenNoPriorActivity()
                .when(new RegisterOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, MISSING_ARGUMENT, ORGANIZATION_STREET,
                        ORGANIZATION_CITY, ORGANIZATION_STATE, ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                        ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS))
                .expectException(ConstraintViolationException.class)
                .expectNoEvents();
    }

    @Test
    void emitsOrganizationDeregisteredByAdminEvent_WhenOrganizationExists() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(new DeregisterOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectEvents(new OrganizationDeregisteredByAdminEvent(ORGANIZATION_ID, ADMIN_ID));
    }

    @Test
    void rejectsOrganizationDeregisterCommand_WhenOrganizationIsAlreadyDeregistered() {
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT, ORGANIZATION_DEREGISTERED_BY_ADMIN_EVENT)
                .when(new DeregisterOrganizationCommand(ORGANIZATION_ID, ADMIN_ID))
                .expectNoEvents()
                .expectException(IllegalStateException.class);
    }

    @Test
    void rejectsOrganizationDeregisteredCommand_WhenRequestingUserIdIsNull() {
        var command = new DeregisterOrganizationCommand(ORGANIZATION_ID, null);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void updateOrganizationCommandEmits_WhenCommandAccepted() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME,
                ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS);
        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectEvents(
                        new OrganizationNameUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_NAME, ADMIN_ID),
                        new OrganizationContactDetailsUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_CONTACT_NAME,
                                ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS, ORGANIZATION_WEBSITE_URL, ADMIN_ID),
                        new OrganizationAddressUpdatedByAdminEvent(ORGANIZATION_ID, ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE, ADMIN_ID));
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenRequestingUserIdIsNull() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, null, ORGANIZATION_NAME,
                ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(ConstraintViolationException.class);
    }

    @Test
    void rejectsUpdateOrganizationCommand_WhenOrganizationIsAlreadyDeregistered() {
        var command = new UpdateOrganizationCommand(ORGANIZATION_ID, ADMIN_ID, ORGANIZATION_NAME,
                ORGANIZATION_STREET, ORGANIZATION_CITY, ORGANIZATION_STATE,
                ORGANIZATION_COUNTRY, ORGANIZATION_POSTAL_CODE,
                ORGANIZATION_WEBSITE_URL, ORGANIZATION_CONTACT_NAME, ORGANIZATION_PHONE_NUMBER, ORGANIZATION_EMAIL_ADDRESS);

        testFixture.given(ORGANIZATION_REGISTERED_BY_ADMIN_EVENT, ORGANIZATION_DEREGISTERED_BY_ADMIN_EVENT)
                .when(command)
                .expectNoEvents()
                .expectException(IllegalStateException.class)
                .expectExceptionMessage("Organization is already deregistered");
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
}
