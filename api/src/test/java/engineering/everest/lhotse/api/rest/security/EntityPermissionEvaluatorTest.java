package engineering.everest.lhotse.api.rest.security;

import engineering.everest.lhotse.axon.common.domain.Identifiable;
import engineering.everest.lhotse.axon.common.domain.User;
import engineering.everest.lhotse.axon.common.services.ReadService;
import engineering.everest.lhotse.axon.common.services.ReadServiceProvider;
import engineering.everest.lhotse.security.AuthenticationContextProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static java.util.UUID.randomUUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntityPermissionEvaluatorTest {

    @Mock
    private AuthenticationContextProvider authenticationContextProvider;
    @Mock
    private ReadServiceProvider readServiceProvider;
    @Mock
    private User user;
    @Mock
    private ReadService<? extends Identifiable> readService;
    @Mock
    private Identifiable identifiable;

    private EntityPermissionEvaluator entityPermissionEvaluator;

    @BeforeEach
    void setUp() {
        when(authenticationContextProvider.getUser()).thenReturn(user);
        Mockito.<ReadService<? extends Identifiable>>when(readServiceProvider.getService(anyString())).thenReturn(readService);
        Mockito.<Identifiable>when(readService.getById(any())).thenReturn(identifiable);
        entityPermissionEvaluator = new EntityPermissionEvaluator(authenticationContextProvider, readServiceProvider);
    }

    @Test
    void hasReadPermissionWillDelegate() {
        entityPermissionEvaluator.hasPermission(null, randomUUID(), "Entity", "read");
        verify(identifiable).canRead(user);
    }

    @Test
    void hasCreatePermissionWillDelegate() {
        entityPermissionEvaluator.hasPermission(null, randomUUID(), "Entity", "create");
        verify(identifiable).canCreate(user);
    }

    @Test
    void hasUpdatePermissionWillDelegate() {
        entityPermissionEvaluator.hasPermission(null, randomUUID(), "Entity", "update");
        verify(identifiable).canUpdate(user);
    }

    @Test
    void hasDeletePermissionWillDelegate() {
        entityPermissionEvaluator.hasPermission(null, randomUUID(), "Entity", "delete");
        verify(identifiable).canDelete(user);
    }

    @Test
    void unknownPermissionWillThrow() {
        assertThrows(IllegalArgumentException.class,
                () -> entityPermissionEvaluator.hasPermission(null, randomUUID(), "Entity", "blah"));
    }
}
