package engineering.everest.lhotse.organizations.domain;

import engineering.everest.lhotse.organizations.domain.commands.RecordSentOrganizationRegistrationEmailConfirmationCommand;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationConfirmedEvent;
import engineering.everest.lhotse.organizations.domain.events.OrganizationRegistrationReceivedEvent;
import engineering.everest.lhotse.users.domain.commands.CreateAdminUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.lhotse.users.domain.events.AdminUserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.starterkit.axon.HazelcastCommandGateway;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.spring.stereotype.Saga;

import java.util.UUID;

@Saga
@Log4j2
@NoArgsConstructor
public class OrganizationRegistrationSaga {

    private UUID organizationId;
    private UUID registeringUserId;
    private String registeringUserEmail;
    private String registeringUserEncodedPassword;
    private String registeringUserDisplayName;

    @StartSaga
    @SagaEventHandler(associationProperty = "organizationId")
    public void on(OrganizationRegistrationReceivedEvent event, HazelcastCommandGateway commandGateway) {
        organizationId = event.getOrganizationId();
        registeringUserId = event.getRegisteringUserId();
        registeringUserEncodedPassword = event.getRegisteringUserEncodedPassword();
        registeringUserDisplayName = event.getContactName();
        registeringUserEmail = event.getRegisteringContactEmail();

        // Add an email to your outbound queue.... maybe record the message ID in the command

        commandGateway.send(new RecordSentOrganizationRegistrationEmailConfirmationCommand(event.getOrganizationId(),
                event.getRegistrationConfirmationCode(), event.getRegisteringContactEmail(), event.getOrganizationName()));
    }

    @SagaEventHandler(associationProperty = "organizationId")
    public void on(OrganizationRegistrationConfirmedEvent event, HazelcastCommandGateway commandGateway) {
        commandGateway.send(new CreateAdminUserForNewlyRegisteredOrganizationCommand(registeringUserId, organizationId,
                registeringUserEmail, registeringUserEncodedPassword, registeringUserDisplayName));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "organizationId")
    public void on(AdminUserCreatedForNewlyRegisteredOrganizationEvent event, HazelcastCommandGateway commandGateway) {
        commandGateway.send(new PromoteUserToOrganizationAdminCommand(organizationId, registeringUserId));
    }
}
