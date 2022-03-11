package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.common.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Map;
import java.util.Set;

import static engineering.everest.lhotse.common.domain.Role.ADMIN;
import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static engineering.everest.lhotse.tasks.AdminUserProvisioningTask.ORGANIZATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class AdminUserProvisioningTaskTest {

    private static final String PASSWORD = "admin-raw-password";
    private static final User USER = new User(ADMIN_ID, ORGANIZATION_ID, "admin@example.com", "Admin", false);

    private AdminUserProvisioningTask adminProvisionTask;

    @Mock
    private SpecialUserProvisioner specialUserProvisioner;

    @BeforeEach
    void setUp() {
        adminProvisionTask = new AdminUserProvisioningTask(specialUserProvisioner, USER.getEmail(), PASSWORD);

        when(specialUserProvisioner.provision(USER, PASSWORD, Set.of(ADMIN))).thenReturn(Map.of("userId", ADMIN_ID));
    }

    @Test
    void run_WillDelegate() {
        assertEquals(Map.of("userId", ADMIN_ID), adminProvisionTask.run());
    }
}
