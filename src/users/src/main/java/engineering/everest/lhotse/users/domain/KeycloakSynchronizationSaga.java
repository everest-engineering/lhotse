package engineering.everest.lhotse.users.domain;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import engineering.everest.lhotse.api.services.KeycloakClient;
import engineering.everest.lhotse.common.RetryWithExponentialBackoff;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesAddedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesRemovedByAdminEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.serialization.Revision;
import org.axonframework.spring.stereotype.Saga;

import java.util.concurrent.Callable;

@Saga
@Revision("0")
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class KeycloakSynchronizationSaga {
    private static final String USER_ID_PROPERTY = "userId";
    private static final String DELETED_USER_ID_PROPERTY = "deletedUserId";

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = USER_ID_PROPERTY)
    public void on(UserRolesAddedByAdminEvent event,
                   KeycloakClient keycloakClient) {
        keycloakClient.addClientLevelUserRoles(event.getUserId(), event.getRoles());
    }

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = USER_ID_PROPERTY)
    public void on(UserRolesRemovedByAdminEvent event,
                   KeycloakClient keycloakClient) {
        keycloakClient.removeClientLevelUserRoles(event.getUserId(), event.getRoles());
    }

    @StartSaga
    @EndSaga
    @SagaEventHandler(associationProperty = DELETED_USER_ID_PROPERTY)
    public void on(UserDeletedAndForgottenEvent event,
                   UsersReadService usersReadService,
                   KeycloakClient keycloakClient)
        throws Exception {
        waitForProjectionUpdate(() -> !usersReadService.exists(event.getDeletedUserId()),
            "user deletion projection update");

        keycloakClient.deleteUser(event.getDeletedUserId());

        // If this user is the last one on the organisation then it might also need to be removed.
        // This isn't catered for in this contrived example.
    }

    // Failure here will result in the saga not completing.
    // Rollback has not been implemented in this example.
    private void waitForProjectionUpdate(Callable<Boolean> condition, String message) throws Exception {
        RetryWithExponentialBackoff.oneMinuteWaiter().waitOrThrow(condition, message);
    }
}
