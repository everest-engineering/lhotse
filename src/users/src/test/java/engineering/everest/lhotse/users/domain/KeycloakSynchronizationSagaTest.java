package engineering.everest.lhotse.users.domain;

import engineering.everest.lhotse.api.services.KeycloakClient;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.common.domain.User;
import engineering.everest.lhotse.users.domain.events.UserDeletedAndForgottenEvent;
import engineering.everest.lhotse.users.domain.events.UserDetailsUpdatedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesAddedByAdminEvent;
import engineering.everest.lhotse.users.domain.events.UserRolesRemovedByAdminEvent;
import engineering.everest.lhotse.users.services.UsersReadService;
import org.axonframework.test.saga.SagaTestFixture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.Role.REGISTERED_USER;
import static java.util.UUID.randomUUID;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class KeycloakSynchronizationSagaTest {
    private SagaTestFixture<KeycloakSynchronizationSaga> testFixture;

    private static final UUID ORGANIZATION_ID = randomUUID();
    private static final UUID REGISTERING_USER_ID = randomUUID();
    private static final UUID REQUESTING_USER_ID = randomUUID();
    private static final String DISPLAY_NAME = "Tester";
    private static final String DISPLAY_NAME_CHANGE = "display name change";
    private static final String EMAIL_ADDRESS = "tester@everest.engineering";
    private static final String EMAIL_ADDRESS_CHANGE = "email address change";
    private static final Set<Role> roles = Set.of(REGISTERED_USER);
    private static final UserRolesAddedByAdminEvent USER_ROLES_ADDED_BY_ADMIN_EVENT =
        new UserRolesAddedByAdminEvent(REGISTERING_USER_ID, roles, REQUESTING_USER_ID);
    private static final UserRolesRemovedByAdminEvent USER_ROLES_REMOVED_BY_ADMIN_EVENT =
        new UserRolesRemovedByAdminEvent(REGISTERING_USER_ID, roles, REQUESTING_USER_ID);

    private static final UserDetailsUpdatedByAdminEvent USER_DETAILS_UPDATED_BY_ADMIN_EVENT =
        new UserDetailsUpdatedByAdminEvent(REGISTERING_USER_ID, ORGANIZATION_ID, DISPLAY_NAME_CHANGE, EMAIL_ADDRESS_CHANGE,
            REQUESTING_USER_ID);
    private static final UserDeletedAndForgottenEvent USER_DELETED_AND_FORGOTTEN_EVENT =
        new UserDeletedAndForgottenEvent(REGISTERING_USER_ID, REQUESTING_USER_ID, "testing");
    private static final User USER = new User(REGISTERING_USER_ID, ORGANIZATION_ID, DISPLAY_NAME, EMAIL_ADDRESS);
    private static final User USER_2 = new User(REGISTERING_USER_ID, ORGANIZATION_ID, DISPLAY_NAME, EMAIL_ADDRESS, false);

    @Mock
    private UsersReadService usersReadService;
    @Mock
    private KeycloakClient keycloakClient;

    @BeforeEach
    void setUp() {
        testFixture = new SagaTestFixture<>(KeycloakSynchronizationSaga.class);
        testFixture.registerResource(usersReadService);
        testFixture.registerResource(keycloakClient);
        testFixture.withTransienceCheckDisabled();
    }

    @Test
    void userRolesAddedByAdminEvent_WillFireAnApiCallToAddRolesInKeycloak() {
        testFixture.givenAggregate(REGISTERING_USER_ID.toString()).published()
            .whenAggregate(REGISTERING_USER_ID.toString()).publishes(USER_ROLES_ADDED_BY_ADMIN_EVENT)
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);

        verify(keycloakClient).addClientLevelUserRoles(USER.getId(), roles);
    }

    @Test
    void userRolesRemovedByAdminEvent_WillFireAnApiCallToRemoveRolesInKeycloak() {
        testFixture.givenAggregate(REGISTERING_USER_ID.toString()).published()
            .whenAggregate(REGISTERING_USER_ID.toString()).publishes(USER_ROLES_REMOVED_BY_ADMIN_EVENT)
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);

        verify(keycloakClient).removeClientLevelUserRoles(USER.getId(), roles);
    }

    // @Test
    // void userDetailsUpdatedByAdminEvent_WillFireAnApiCallToUpdateDetailsInKeycloak() {
    // when(usersReadService.getById(USER_2.getId()))
    // .thenReturn(new User(USER_2.getId(), USER_2.getOrganizationId(), DISPLAY_NAME_CHANGE, EMAIL_ADDRESS_CHANGE));
    //
    // testFixture.givenAggregate(REGISTERING_USER_ID.toString()).published()
    // .whenAggregate(REGISTERING_USER_ID.toString()).publishes(USER_DETAILS_UPDATED_BY_ADMIN_EVENT)
    // .expectNoDispatchedCommands()
    // .expectActiveSagas(0);
    //
    // verify(keycloakSynchronizationService).updateUserAttributes(USER_2.getId(),
    // Map.of("attributes", new UserAttribute(DISPLAY_NAME_CHANGE),
    // "email", EMAIL_ADDRESS_CHANGE));
    // }

    @Test
    void userDeletedAndForgottenEvent_WillFireAnApiCallToDeleteUserFromKeycloak() {
        testFixture.givenAggregate(REGISTERING_USER_ID.toString()).published()
            .whenAggregate(REGISTERING_USER_ID.toString()).publishes(USER_DELETED_AND_FORGOTTEN_EVENT)
            .expectNoDispatchedCommands()
            .expectActiveSagas(0);

        verify(keycloakClient).deleteUser(USER.getId());
    }
}
