package engineering.everest.starterkit;

import engineering.everest.starterkit.axon.common.PasswordEncoder;
import engineering.everest.starterkit.users.persistence.PersistableUser;
import engineering.everest.starterkit.users.persistence.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Optional;
import java.util.UUID;

import static engineering.everest.starterkit.axon.common.domain.Role.ADMIN;
import static java.util.UUID.fromString;

@Component
public class AdminProvisionTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminProvisionTask.class);
    private static final UUID ADMIN_ID = fromString("00000000-0000-0000-0000-000000000000");
    private static final String ADMIN_DISPLAY_NAME = "Admin";

    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;

    public AdminProvisionTask(UsersRepository usersRepository,
                              PasswordEncoder passwordEncoder,
                              @Value("admin") String adminUsername,
                              @Value("ac0n3x72") String adminPassword) {
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
    }

    @PostConstruct
    public void run() {
        final Optional<PersistableUser> adminUser = usersRepository.findByUsernameIgnoreCase(adminUsername);
        if (adminUser.isPresent()) {
            LOGGER.info("Skipping provisioning of admin account since it already exists");
            return;
        }

        LOGGER.info("Provisioning admin user");
        usersRepository.save(new PersistableUser(ADMIN_ID, null, adminUsername,
                passwordEncoder.encode(adminPassword),
                ADMIN_DISPLAY_NAME, false, EnumSet.of(ADMIN), Instant.now()));
    }
}
