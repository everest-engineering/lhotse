package engineering.everest.lhotse.users.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import engineering.everest.lhotse.axon.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.axon.common.domain.UserAttribute;
import engineering.everest.lhotse.axon.common.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesUpdatedByAdminEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

@Saga
@Revision("0")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class KeycloakSynchronizationSaga {
    private static final String USER_ID_PROPERTY = "userId";
    private static final String DELETED_USER_ID_PROPERTY = "deletedUserId";

    @JsonIgnore
    private transient UsersReadService usersReadService;

    @Autowired
    private KeycloakSynchronizationService keycloakSynchronizationService;

    @Autowired
    public void setUsersReadService(UsersReadService usersReadService) {
        this.usersReadService = usersReadService;
    }

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = USER_ID_PROPERTY)
    public void on(UserRolesUpdatedByAdminEvent event) throws Exception {
        var user = usersReadService.getById(event.getUserId());

        waitForTheProjectionUpdate(() -> user.getRoles().equals(event.getRoles()),
                "user roles projection update");

        keycloakSynchronizationService.updateUserAttributes(event.getUserId(),
                Map.of("attributes", new UserAttribute(user.getOrganizationId(), event.getRoles(), user.getDisplayName())));
    }

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = DELETED_USER_ID_PROPERTY)
    public void on(UserDeletedAndForgottenEvent event) throws Exception {
        waitForTheProjectionUpdate(() -> !usersReadService.exists(event.getDeletedUserId()),
                "user deletion projection update");

        keycloakSynchronizationService.deleteUser(event.getDeletedUserId());
    }

    // Failure here will result in the saga not completing.
    // Rollback has not been implemented in this example.
    private void waitForTheProjectionUpdate(Callable<Boolean> condition, String message) throws Exception {
        new RetryWithExponentialBackoff(Duration.ofMillis(200), 2L, Duration.ofMinutes(1),
                x -> MILLISECONDS.sleep(x.toMillis())).waitOrThrow(condition, message);
    }
}
