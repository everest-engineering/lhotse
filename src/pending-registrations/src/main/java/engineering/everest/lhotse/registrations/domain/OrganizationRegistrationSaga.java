package engineering.everest.lhotse.registrations.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
import engineering.everest.lhotse.users.services.UsersReadService;
import engineering.everest.starterkit.axon.cryptoshredding.annotations.EncryptedField;
import engineering.everest.starterkit.axon.cryptoshredding.annotations.EncryptionKeyIdentifier;
import lombok.extern.log4j.Log4j2;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

@Saga
@Revision("0")
@Log4j2
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY) // TODO there might be a cleaner way
public class OrganizationRegistrationSaga {
    private static final String CONFIRMATION_CODE = "registrationConfirmationCode";

    @JsonIgnore
    private transient CommandGateway commandGateway;
    @JsonIgnore
    private transient UsersReadService usersReadService;

    private UUID organizationId;
    @EncryptionKeyIdentifier
    private UUID registeringUserId;
    @EncryptedField
    private String registeringUserEmail;
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

    @StartSaga
    @SagaEventHandler(associationProperty = CONFIRMATION_CODE)
    public void on(OrganizationRegistrationReceivedEvent event) {
        organizationId = event.getOrganizationId();
        registeringUserId = event.getRegisteringUserId();
        registeringUserEncodedPassword = event.getRegisteringUserEncodedPassword();
        registeringUserDisplayName = event.getContactName();
        registeringUserEmail = event.getRegisteringContactEmail();
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

    @SagaEventHandler(associationProperty = CONFIRMATION_CODE)
    public void on(OrganizationRegistrationConfirmedEvent event) {
        // The business may require that each email address be only available for registration once. In this case,
        // the target aggregate identifier may be the user email address (possibly with an additional prefix).
        if (usersReadService.hasUserWithEmail(registeringUserEmail)) {
            commandGateway.send(new CancelConfirmedRegistrationUserEmailAlreadyInUseCommand(
                    event.getRegistrationConfirmationCode(), organizationId, registeringUserId, registeringUserEmail));
        } else {
            var registeredOrganizationCommand = new CreateRegisteredOrganizationCommand(organizationId, registeringUserId, organizationName,
                    street, city, state, country, postalCode, websiteUrl, registeringUserDisplayName, phoneNumber, registeringUserEmail);
            var createUserCommand = new CreateUserForNewlyRegisteredOrganizationCommand(registeringUserId, organizationId,
                    event.getRegistrationConfirmationCode(), registeringUserEmail, registeringUserEncodedPassword,
                    registeringUserDisplayName);
            var promoteUserToOrganizationAdminCommand = new PromoteUserToOrganizationAdminCommand(organizationId, registeringUserId);
            var completeRegistrationCommand = new CompleteOrganizationRegistrationCommand(event.getRegistrationConfirmationCode(),
                    organizationId, registeringUserId);

            commandGateway.sendAndWait(registeredOrganizationCommand);
            commandGateway.sendAndWait(createUserCommand);
            commandGateway.sendAndWait(promoteUserToOrganizationAdminCommand);
            commandGateway.send(completeRegistrationCommand);
        }
    }

    @EndSaga
    @SagaEventHandler(associationProperty = CONFIRMATION_CODE)
    public void on(OrganizationRegistrationCompletedEvent event) {
    }

    @EndSaga
    @SagaEventHandler(associationProperty = CONFIRMATION_CODE)
    public void on(OrganizationRegistrationConfirmedAfterUserWithEmailCreatedEvent event) {
    }
}
