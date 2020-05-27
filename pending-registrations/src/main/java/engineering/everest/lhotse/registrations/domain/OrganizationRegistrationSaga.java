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
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.UUID;

@Saga
@Log4j2
@NoArgsConstructor
public class OrganizationRegistrationSaga {
    private static final String ASSOCIATION_PROPERTY = "registrationConfirmationCode";

    private transient CommandGateway commandGateway;
    private transient UsersReadService usersReadService;

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
    @SagaEventHandler(associationProperty = ASSOCIATION_PROPERTY)
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

    @SagaEventHandler(associationProperty = ASSOCIATION_PROPERTY)
    public void on(OrganizationRegistrationConfirmedEvent event) {
        // The business may require that each email address be only available for registration once. In this case,
        // the target aggregate identifier may be the user email address (possibly with an additional prefix).
        if (usersReadService.hasUserWithEmail(registeringUserEmail)) {
            commandGateway.send(new CancelConfirmedRegistrationUserEmailAlreadyInUseCommand(
                    event.getRegistrationConfirmationCode(), organizationId, registeringUserId, registeringUserEmail));
        } else {
            commandGateway.send(new CreateRegisteredOrganizationCommand(organizationId, registeringUserId, organizationName,
                    street, city, state, country, postalCode, websiteUrl, registeringUserDisplayName, phoneNumber, registeringUserEmail));

            commandGateway.send(new CreateUserForNewlyRegisteredOrganizationCommand(registeringUserId, organizationId,
                    event.getRegistrationConfirmationCode(), registeringUserEmail, registeringUserEncodedPassword,
                    registeringUserDisplayName));
        }
    }

    @SagaEventHandler(associationProperty = ASSOCIATION_PROPERTY)
    public void on(UserCreatedForNewlyRegisteredOrganizationEvent event) {
        commandGateway.send(new PromoteUserToOrganizationAdminCommand(organizationId, registeringUserId));
        commandGateway.send(new CompleteOrganizationRegistrationCommand(event.getRegistrationConfirmationCode(),
                organizationId, registeringUserId));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = ASSOCIATION_PROPERTY)
    public void on(OrganizationRegistrationCompletedEvent event) {
    }

    @EndSaga
    @SagaEventHandler(associationProperty = ASSOCIATION_PROPERTY)
    public void on(OrganizationRegistrationConfirmedAfterUserWithEmailCreatedEvent event) {
    }
}
