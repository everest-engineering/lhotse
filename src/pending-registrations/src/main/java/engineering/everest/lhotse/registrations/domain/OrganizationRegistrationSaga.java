package engineering.everest.lhotse.registrations.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import engineering.everest.axon.cryptoshredding.annotations.EncryptedField;
import engineering.everest.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import engineering.everest.lhotse.axon.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegisteredEvent;
import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.registrations.domain.commands.CancelConfirmedRegistrationUserEmailAlreadyInUseCommand;
import engineering.everest.lhotse.registrations.domain.commands.CompleteOrganizationRegistrationCommand;
import engineering.everest.lhotse.registrations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCancelledUserWithEmailAddressAlreadyInUseEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationCompletedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import lombok.extern.log4j.Log4j2;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.time.Duration;
import java.util.UUID;
import java.util.concurrent.Callable;

@Saga
@Revision("0")
@Log4j2
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // TODO there might be a cleaner way
public class OrganizationRegistrationSaga {
    private static final String CONFIRMATION_CODE_PROPERTY = "registrationConfirmationCode";
    private static final String REGISTERING_USER_PROPERTY = "registeringUserId";
    private static final String ORGANIZATION_PROPERTY = "organizationId";

    @JsonIgnore
    private transient CommandGateway commandGateway;
    @JsonIgnore
    private transient UsersReadService usersReadService;
    @JsonIgnore
    private transient OrganizationsReadService organizationsReadService;

    private UUID organizationId;
    @EncryptionKeyIdentifier
    private UUID registeringUserId;
    @EncryptedField
    private String registeringUserEmail;
    private UUID registrationConfirmationCode;
    private String registeringUserEncodedPassword;
    @EncryptedField
    private String registeringUserDisplayName;
    private String organizationName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String websiteUrl;
    @EncryptedField
    private String phoneNumber;

    // Not implemented: deadline management for handling confirmation timeouts

    @Autowired
    @Qualifier("hazelcastCommandGateway")
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @Autowired
    public void setUsersReadService(UsersReadService usersReadService) {
        this.usersReadService = usersReadService;
    }

    @Autowired
    public void setOrganizationsReadService(OrganizationsReadService organizationsReadService) {
        this.organizationsReadService = organizationsReadService;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = CONFIRMATION_CODE_PROPERTY)
    public void on(OrganizationRegistrationReceivedEvent event) {
        organizationId = event.getOrganizationId();
        registeringUserId = event.getRegisteringUserId();
        registeringUserEncodedPassword = event.getRegisteringUserEncodedPassword();
        registeringUserDisplayName = event.getContactName();
        registeringUserEmail = event.getRegisteringContactEmail();
        registrationConfirmationCode = event.getRegistrationConfirmationCode();
        organizationName = event.getOrganizationName();
        street = event.getStreet();
        city = event.getCity();
        state = event.getState();
        country = event.getCountry();
        postalCode = event.getPostalCode();
        websiteUrl = event.getWebsiteUrl();
        phoneNumber = event.getContactPhoneNumber();

        // Add an email to your outbound queue.... maybe record the message ID in the command

        commandGateway.send(new RecordSentOrganizationRegistrationEmailConfirmationCommand(event.getRegistrationConfirmationCode(),
                event.getOrganizationId(), event.getRegisteringContactEmail(), event.getOrganizationName(), event.getRegisteringUserId()));
    }

    @SagaEventHandler(associationProperty = CONFIRMATION_CODE_PROPERTY)
    public void on(OrganizationRegistrationConfirmedEvent event) {
        SagaLifecycle.associateWith(ORGANIZATION_PROPERTY, organizationId.toString());
        SagaLifecycle.associateWith(REGISTERING_USER_PROPERTY, registeringUserId.toString());

        // The business may require that each email address be only available for registration once. In this case,
        // the target aggregate identifier may be the user email address (possibly with an additional prefix).
        if (usersReadService.hasUserWithEmail(registeringUserEmail)) {
            commandGateway.send(new CancelConfirmedRegistrationUserEmailAlreadyInUseCommand(
                    event.getRegistrationConfirmationCode(), organizationId, registeringUserId, registeringUserEmail));
        } else {
            commandGateway.send(new CreateRegisteredOrganizationCommand(organizationId, registeringUserId, organizationName,
                    street, city, state, country, postalCode, websiteUrl, registeringUserDisplayName, phoneNumber, registeringUserEmail));
        }
    }

    @SagaEventHandler(associationProperty = REGISTERING_USER_PROPERTY)
    public void on(OrganizationRegisteredEvent event) {
        commandGateway.send(new CreateUserForNewlyRegisteredOrganizationCommand(registeringUserId, organizationId,
                registeringUserEmail, registeringUserEncodedPassword, registeringUserDisplayName));
    }

    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(UserCreatedForNewlyRegisteredOrganizationEvent event) throws Exception {
        var retryWithExponentialBackoff = new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, Duration.ofMinutes(1));
        Callable<Boolean> projectionsDone = () -> usersReadService.exists(event.getUserId())
                && organizationsReadService.exists(event.getOrganizationId());

        // Failure here will result in the saga not completing. Rollback has not been implemented in this example.
        retryWithExponentialBackoff.waitOrThrow(projectionsDone, "user and organization self registration projection update");
        commandGateway.send(new PromoteUserToOrganizationAdminCommand(organizationId, registeringUserId));
    }

    @SagaEventHandler(associationProperty = "promotedUserId", keyName = REGISTERING_USER_PROPERTY)
    public void on(UserPromotedToOrganizationAdminEvent event) {
        commandGateway.send(new CompleteOrganizationRegistrationCommand(registrationConfirmationCode, organizationId, registeringUserId));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(OrganizationRegistrationCompletedEvent event) {
    }

    @EndSaga
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(OrganizationRegistrationCancelledUserWithEmailAddressAlreadyInUseEvent event) {
    }
}
