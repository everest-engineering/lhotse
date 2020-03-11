package engineering.everest.lhotse;

import engineering.everest.lhotse.axon.replay.ReplayAware;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.extern.log4j.Log4j2;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Clock;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static engineering.everest.lhotse.axon.common.domain.Role.ADMIN;
import static java.util.UUID.fromString;

@Component
@Log4j2
public class AdminProvisionTask implements ReplayAware {

    private static final UUID ADMIN_ID = fromString("00000000-0000-0000-0000-000000000000");
    private static final String ADMIN_DISPLAY_NAME = "Admin";

    private final Clock clock;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminProvisionTask(Clock clock,
                              UsersRepository usersRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("${application.setup.admin.username}") String adminUsername,
                              @Value("${application.setup.admin.password}") String adminPassword) {
        this.clock = clock;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public void run() {
        Optional<PersistableUser> adminUser = usersRepository.findByUsernameIgnoreCase(adminUsername);

        if (adminUser.isPresent()) {
            LOGGER.info("Skipping provisioning of admin account since it already exists");
            return;
        }

        LOGGER.info("Provisioning admin user");
        usersRepository.save(new PersistableUser(ADMIN_ID, null, adminUsername, passwordEncoder.encode(adminPassword),
                ADMIN_DISPLAY_NAME, false, EnumSet.of(ADMIN), Instant.now(clock)));
    }

    @Override
    @EventHandler
    public void replayCompleted() {
        run();
    }
}
