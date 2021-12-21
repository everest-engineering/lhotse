package engineering.everest.lhotse.registrations.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.common.domain.UserAttribute;
import engineering.everest.lhotse.organizations.domain.events.OrganizationCreatedForNewSelfRegisteredUserEvent;
import engineering.everest.lhotse.organizations.domain.events.UserPromotedToOrganizationAdminEvent;
import engineering.everest.lhotse.organizations.services.OrganizationsReadService;
import engineering.everest.lhotse.users.domain.commands.CreateUserForNewlyRegisteredOrganizationCommand;
import engineering.everest.lhotse.users.domain.commands.PromoteUserToOrganizationAdminCommand;
import engineering.everest.lhotse.users.domain.events.UserCreatedForNewlyRegisteredOrganizationEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Map;
import java.util.Set;

import static java.util.Map.entry;

@Saga
@Revision("0")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OrganizationRegistrationSaga {
    private static final String ORGANIZATION_PROPERTY = "organizationId";

    @JsonIgnore
    private transient CommandGateway commandGateway;

    @Autowired
    @Qualifier("hazelcastCommandGateway")
    public void setCommandGateway(CommandGateway commandGateway) {
        this.commandGateway = commandGateway;
    }

    @StartSaga
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(OrganizationCreatedForNewSelfRegisteredUserEvent event) {
        var organizationId = event.getOrganizationId();
        var registeringUserId = event.getRegisteringUserId();
        var registeringUserDisplayName = event.getContactName();
        var registeringUserEmail = event.getContactEmail();

        commandGateway.send(new CreateUserForNewlyRegisteredOrganizationCommand(organizationId, registeringUserId,
            registeringUserEmail, registeringUserDisplayName));
    }

    // Failure here will result in the saga not completing.
    // Rollback has not been implemented in this example.
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(UserCreatedForNewlyRegisteredOrganizationEvent event,
                   UsersReadService usersReadService,
                   OrganizationsReadService organizationsReadService)
        throws Exception {
        RetryWithExponentialBackoff.oneMinuteWaiter().waitOrThrow(() -> usersReadService.exists(event.getUserId())
            && organizationsReadService.exists(event.getOrganizationId()),
            "user and organization self registration projection update");

        commandGateway.send(new PromoteUserToOrganizationAdminCommand(event.getOrganizationId(), event.getUserId()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(UserPromotedToOrganizationAdminEvent event,
                   UsersReadService usersReadService,
                   KeycloakSynchronizationService keycloakSynchronizationService) {

        keycloakSynchronizationService.updateUserAttributes(event.getPromotedUserId(),
            Map.ofEntries(entry("attributes", new UserAttribute(event.getOrganizationId(),
                usersReadService.getById(event.getPromotedUserId()).getDisplayName()))));

        keycloakSynchronizationService.addClientLevelUserRoles(event.getPromotedUserId(), Set.of(Role.ORG_ADMIN));
    }
}
