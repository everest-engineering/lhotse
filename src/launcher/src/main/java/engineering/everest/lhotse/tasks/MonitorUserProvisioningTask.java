package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.common.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.Role.MONITORING;
import static engineering.everest.lhotse.common.domain.User.MONITORING_ID;
import static java.util.UUID.fromString;

@Component
@Slf4j
public class MonitorUserProvisioningTask implements ReplayCompletionAware {

    public static final UUID ORGANIZATION_ID = fromString("00000000-0000-0000-0000-000000000000");

    private final SpecialUserProvisioner specialUserProvisioner;
    private final String email;
    private final String password;

    public MonitorUserProvisioningTask(SpecialUserProvisioner specialUserProvisioner,
                                       @Value("${kc.server.monitoring-user}") String email,
                                       @Value("${kc.server.monitoring-password}") String password) {
        this.specialUserProvisioner = specialUserProvisioner;
        this.email = email;
        this.password = password;
    }

    @PostConstruct
    public Map<String, Object> run() {
        return specialUserProvisioner.provision(new User(MONITORING_ID, ORGANIZATION_ID, email, "Monitoring"), password,
            Set.of(MONITORING));
    }

    @Override
    public void replayCompleted() {
        run();
    }
}
