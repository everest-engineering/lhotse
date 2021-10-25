package engineering.everest.lhotse.registrations.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import engineering.everest.lhotse.axon.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.domain.UserAttribute;
import engineering.everest.lhotse.axon.common.services.KeycloakSynchronizationService;
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

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.Map.entry;

@Saga
@Revision("0")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class OrganizationRegistrationSaga {
    private static final String ORGANIZATION_PROPERTY = "organizationId";

    @JsonIgnore
    private transient CommandGateway commandGateway;
    @JsonIgnore
    private transient UsersReadService usersReadService;
    @JsonIgnore
    private transient OrganizationsReadService organizationsReadService;

    @Autowired
    private KeycloakSynchronizationService keycloakSynchronizationService;

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
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(OrganizationCreatedForNewSelfRegisteredUserEvent event) {
        var organizationId = event.getOrganizationId();
        var registeringUserId = event.getRegisteringUserId();
        var registeringUserDisplayName = event.getContactName();
        var registeringUserEmail = event.getContactEmail();

        commandGateway.send(new CreateUserForNewlyRegisteredOrganizationCommand(organizationId, registeringUserId,
                registeringUserEmail, registeringUserDisplayName));
    }

    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(UserCreatedForNewlyRegisteredOrganizationEvent event) throws Exception {
        waitForTheProjectionUpdate(
                () -> usersReadService.exists(event.getUserId())
                        && organizationsReadService.exists(event.getOrganizationId()),
                "user and organization self registration projection update");

        commandGateway.send(new PromoteUserToOrganizationAdminCommand(event.getOrganizationId(), event.getUserId()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = ORGANIZATION_PROPERTY)
    public void on(UserPromotedToOrganizationAdminEvent event) throws Exception {
        waitForTheProjectionUpdate(
                () -> usersReadService.getById(event.getPromotedUserId()).getRoles().contains(Role.ORG_ADMIN),
                "user roles projection update");

        keycloakSynchronizationService.updateUserAttributes(event.getPromotedUserId(), Map.ofEntries(entry("attributes",
                new UserAttribute(event.getOrganizationId(), usersReadService.getById(event.getPromotedUserId()).getRoles(),
                        usersReadService.getById(event.getPromotedUserId()).getDisplayName()))));
    }

    // Failure here will result in the saga not completing.
    // Rollback has not been implemented in this example.
    private void waitForTheProjectionUpdate(Callable<Boolean> condition, String message) throws Exception {
        new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, Duration.ofMinutes(1),
                x -> MILLISECONDS.sleep(x.toMillis())).waitOrThrow(condition, message);
    }
}
