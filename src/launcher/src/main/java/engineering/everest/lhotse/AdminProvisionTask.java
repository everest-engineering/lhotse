package engineering.everest.lhotse;

import engineering.everest.lhotse.axon.common.domain.Role;
import engineering.everest.lhotse.axon.common.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;

import static engineering.everest.lhotse.axon.common.domain.Role.ORG_USER;
import static engineering.everest.lhotse.axon.common.domain.Role.ORG_ADMIN;
import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
import static java.util.UUID.fromString;

@Component
@Log4j2
public class AdminProvisionTask implements ReplayCompletionAware {
    public static final UUID ORGANIZATION_ID = fromString("00000000-0000-0000-0000-000000000000");
    private static final String ADMIN_DISPLAY_NAME = "Admin";

    private final Clock clock;
    private final UsersRepository usersRepository;
    private final KeycloakSynchronizationService keycloakSynchronizationService;
    private final String adminUsername;
    private final String adminPassword;

    public AdminProvisionTask(Clock clock, UsersRepository usersRepository,
                              KeycloakSynchronizationService keycloakSynchronizationService,
                              @Value("${kc.server.admin-user}") String adminUsername,
                              @Value("${kc.server.admin-password}") String adminPassword) {
        this.clock = clock;
        this.usersRepository = usersRepository;
        this.keycloakSynchronizationService = keycloakSynchronizationService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public Map<String, Object> run() {
        var userDetails = keycloakSynchronizationService.setupKeycloakUser(adminUsername, adminUsername, true, ORGANIZATION_ID,
                Set.of(Role.ORG_USER, Role.ORG_ADMIN), ADMIN_DISPLAY_NAME, adminPassword, false);

        Optional<PersistableUser> adminUser = usersRepository.findByUsernameIgnoreCase(adminUsername);
        if (adminUser.isPresent()) {
            LOGGER.info("Skipping provisioning of admin account since it already exists");
            return userDetails;
        }

        LOGGER.info("Provisioning admin user");
        usersRepository.save(new PersistableUser(UUID.fromString(userDetails.getOrDefault("userId", ADMIN_ID).toString()),
                ORGANIZATION_ID, adminUsername, ADMIN_DISPLAY_NAME, false, EnumSet.of(ORG_USER, ORG_ADMIN), Instant.now(clock)));

        return userDetails;
    }

    @Override
    public void replayCompleted() {
        run();
    }
}
