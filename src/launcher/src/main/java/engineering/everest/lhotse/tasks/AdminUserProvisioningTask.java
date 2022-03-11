package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.common.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static java.util.UUID.fromString;

@Component
@Slf4j
public class AdminUserProvisioningTask implements ReplayCompletionAware {
    public static final UUID ORGANIZATION_ID = fromString("00000000-0000-0000-0000-000000000000");

    private final SpecialUserProvisioner specialUserProvisioner;
    private final String adminEmail;
    private final String adminPassword;

    public AdminUserProvisioningTask(SpecialUserProvisioner specialUserProvisioner,
                                     @Value("${kc.server.admin-user}") String adminUsername,
                                     @Value("${kc.server.admin-password}") String adminPassword) {
        this.specialUserProvisioner = specialUserProvisioner;
        this.adminEmail = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public Map<String, Object> run() {
        return specialUserProvisioner.provision(new User(ADMIN_ID, ORGANIZATION_ID, adminEmail, "Admin"), adminPassword,
            Set.of(Role.ADMIN));
    }

    @Override
    public void replayCompleted() {
        run();
    }
}
