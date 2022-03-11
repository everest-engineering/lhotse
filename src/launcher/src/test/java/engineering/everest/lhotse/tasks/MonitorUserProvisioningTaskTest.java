package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.common.domain.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.Set;

import static engineering.everest.lhotse.common.domain.Role.MONITORING;
import static engineering.everest.lhotse.common.domain.User.MONITORING_ID;
import static engineering.everest.lhotse.tasks.MonitorUserProvisioningTask.ORGANIZATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorUserProvisioningTaskTest {

    private static final String PASSWORD = "monitoring-raw-password";
    private static final User USER = new User(MONITORING_ID, ORGANIZATION_ID, "monitoring@example.com", "Monitoring", false);

    private MonitorUserProvisioningTask monitorUserProvisioningTask;

    @Mock
    private SpecialUserProvisioner specialUserProvisioner;

    @BeforeEach
    void setUp() {
        monitorUserProvisioningTask = new MonitorUserProvisioningTask(specialUserProvisioner, USER.getEmail(), PASSWORD);

        when(specialUserProvisioner.provision(USER, PASSWORD, Set.of(MONITORING))).thenReturn(Map.of("userId", MONITORING_ID));
    }

    @Test
    void run_WillDelegate() {
        assertEquals(Map.of("userId", MONITORING_ID), monitorUserProvisioningTask.run());
    }
}
