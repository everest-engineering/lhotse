package engineering.everest.lhotse;

import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.axon.replay.ReplayCompletionAware;
import engineering.everest.lhotse.organizations.persistence.Address;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.organizations.persistence.PersistableOrganization;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import javax.annotation.PostConstruct;

import static engineering.everest.lhotse.axon.common.domain.Role.ADMIN;
import static engineering.everest.lhotse.axon.common.domain.Role.ORG_ADMIN;
import static engineering.everest.lhotse.axon.common.domain.Role.ORG_USER;
import static engineering.everest.lhotse.axon.common.domain.User.ADMIN_ID;
import static java.util.UUID.fromString;

@Component
@Log4j2
public class AdminProvisionTask implements ReplayCompletionAware {
    public static final UUID ORGANIZATION_ID = fromString("00000000-0000-0000-0000-000000000000");
    private static final String ADMIN_DISPLAY_NAME = "Admin";
    private static final String ORGANIZATION_NAME = "Admin Org";
    private static final boolean ORGANIZATION_DISABLED = false;

    private final Clock clock;
    private final UsersRepository usersRepository;
    private final OrganizationsRepository organizationsRepository;
    private final KeycloakSynchronizationService keycloakSynchronizationService;
    private final String adminUsername;
    private final String adminPassword;

    public AdminProvisionTask(Clock clock, UsersRepository usersRepository,
                              OrganizationsRepository organizationsRepository,
                              KeycloakSynchronizationService keycloakSynchronizationService,
                              @Value("${kc.server.admin-user}") String adminUsername,
                              @Value("${kc.server.admin-password}") String adminPassword) {
        this.clock = clock;
        this.usersRepository = usersRepository;
        this.organizationsRepository = organizationsRepository;
        this.keycloakSynchronizationService = keycloakSynchronizationService;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public Map<String, Object> run() {
        var userDetails = keycloakSynchronizationService.setupKeycloakUser(adminUsername, adminUsername, true, ORGANIZATION_ID,
                Set.of(ORG_USER, ORG_ADMIN, ADMIN), ADMIN_DISPLAY_NAME, adminPassword, false);

        Optional<PersistableUser> adminUser = usersRepository.findByUsernameIgnoreCase(adminUsername);
        if (adminUser.isPresent()) {
            LOGGER.info("Skipping provisioning of admin account since it already exists");
        } else {
            LOGGER.info("Provisioning admin organization");
            organizationsRepository.save(new PersistableOrganization(ORGANIZATION_ID, ORGANIZATION_NAME,
                    new Address(null, null, null, null, null), null,
                    ADMIN_DISPLAY_NAME, null, adminUsername, ORGANIZATION_DISABLED, Instant.now(clock)));

            LOGGER.info("Provisioning admin user");
            usersRepository.save(new PersistableUser(fromString(userDetails.getOrDefault("userId", ADMIN_ID).toString()),
                    ORGANIZATION_ID, adminUsername, ADMIN_DISPLAY_NAME, false, Instant.now(clock)));
        }
        return userDetails;
    }

    @Override
    public void replayCompleted() {
        run();
    }
}
