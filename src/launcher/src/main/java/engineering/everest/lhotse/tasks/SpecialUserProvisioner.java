package engineering.everest.lhotse.tasks;

import engineering.everest.lhotse.api.services.KeycloakSynchronizationService;
import engineering.everest.lhotse.common.domain.Role;
import engineering.everest.lhotse.common.domain.User;
import engineering.everest.lhotse.organizations.persistence.Address;
import engineering.everest.lhotse.organizations.persistence.OrganizationsRepository;
import engineering.everest.lhotse.organizations.persistence.PersistableOrganization;
import engineering.everest.lhotse.users.persistence.PersistableUser;
import engineering.everest.lhotse.users.persistence.UsersRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.common.domain.User.ADMIN_ID;
import static engineering.everest.lhotse.tasks.AdminUserProvisioningTask.ORGANIZATION_ID;

@Component
@Slf4j
public class SpecialUserProvisioner {

    private final Clock clock;
    private final UsersRepository usersRepository;
    private final OrganizationsRepository organizationsRepository;
    private final KeycloakSynchronizationService keycloakSynchronizationService;

    public SpecialUserProvisioner(Clock clock,
                                  UsersRepository usersRepository,
                                  OrganizationsRepository organizationsRepository,
                                  KeycloakSynchronizationService keycloakSynchronizationService) {
        this.clock = clock;
        this.usersRepository = usersRepository;
        this.organizationsRepository = organizationsRepository;
        this.keycloakSynchronizationService = keycloakSynchronizationService;
    }

    public Map<String, Object> provision(User user, String clearTextPassword, Set<Role> roles) {
        var userDetails =
            keycloakSynchronizationService.setupKeycloakUser(user.getEmailAddress(), !user.isDisabled(),
                user.getOrganizationId(), roles, user.getDisplayName(), clearTextPassword, false);

        var existingUser = usersRepository.findByEmailAddressIgnoreCase(user.getEmailAddress());
        if (existingUser.isPresent()) {
            LOGGER.info("Skipping provisioning of '{}' account since it already exists", user.getDisplayName());
        } else {
            if (!organizationsRepository.existsById(ORGANIZATION_ID)) {
                LOGGER.info("Provisioning admin organization");
                organizationsRepository.save(new PersistableOrganization(ORGANIZATION_ID, "Admin Org",
                    new Address(null, null, null, null, null), null,
                    user.getDisplayName(), null, user.getEmailAddress(), false, Instant.now(clock)));
            }

            LOGGER.info("Provisioning user '{}'", user.getDisplayName());
            var userId = UUID.fromString(userDetails.getOrDefault("userId", ADMIN_ID).toString());
            usersRepository.save(new PersistableUser(userId, user.getOrganizationId(), user.getDisplayName(),
                user.getEmailAddress(), user.isDisabled(), Instant.now(clock)));
        }
        return userDetails;
    }
}
