package engineering.everest.lhotse.registrations.domain;

import engineering.everest.lhotse.organizations.domain.commands.CreateRegisteredOrganizationCommand;
import engineering.everest.lhotse.registrations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.registrations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.starterkit.axon.HazelcastCommandGateway;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.commandhandling.gateway.DefaultCommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

@Saga
@Log4j2
@NoArgsConstructor
public class OrganizationRegistrationSaga {
    private transient CommandGateway commandGateway;

    private UUID organizationId;
    private UUID registeringUserId;
    private String registeringUserEmail;
    private String registeringUserEncodedPassword;
    private String registeringUserDisplayName;
    private String organizationName;
    private String street;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String websiteUrl;
    private String phoneNumber;

    @Autowired
    public void setDefaultCommandGateway(DefaultCommandGateway commandGateway) {
        if (this.commandGateway == null) { // Yuck
            this.commandGateway = commandGateway;
        }
    }

    @Autowired
    public void setHazelcastCommandGateway(HazelcastCommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = "registrationConfirmationCode")
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
                event.getOrganizationId(), event.getRegisteringContactEmail(), event.getOrganizationName()));
    }

    @SagaEventHandler(associationProperty = "registrationConfirmationCode")
    public void on(OrganizationRegistrationConfirmedEvent event) {
        commandGateway.send(new CreateRegisteredOrganizationCommand(organizationId, registeringUserId, organizationName,
                street, city, state, country, postalCode, websiteUrl, registeringUserDisplayName, phoneNumber, registeringUserEmail));

        commandGateway.send(new CreateUserForNewlyRegisteredOrganizationCommand(registeringUserId, organizationId,
                event.getRegistrationConfirmationCode(), registeringUserEmail, registeringUserEncodedPassword, registeringUserDisplayName));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "registrationConfirmationCode")
    public void on(UserCreatedForNewlyRegisteredOrganizationEvent event) {
        commandGateway.send(new PromoteUserToOrganizationAdminCommand(organizationId, registeringUserId));
    }
}
