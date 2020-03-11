package engineering.everest.lhotse.axon.common.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static engineering.everest.lhotse.axon.common.domain.Role.ADMIN;
import static engineering.everest.lhotse.axon.common.domain.Role.ORG_ADMIN;
import static engineering.everest.lhotse.axon.common.domain.Role.ORG_USER;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuppressWarnings("PMD.ShortClassName")
public class User implements Identifiable {

    private UUID id;
    private UUID organizationId;
    private String username;
    private String displayName;
    private String email;
    private boolean disabled;
    private Set<Role> roles;

    public User(UUID id, UUID organizationId, String username, String displayName) {
        this(id, organizationId, username, displayName, false);
    }

    public User(UUID id, UUID organizationId, String username, String displayName, boolean disabled) {
        this(id, organizationId, username, displayName, username, disabled, Set.of(ORG_USER));
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public boolean hasAnyRole(Role... roles) {
        return Arrays.stream(roles).anyMatch(this::hasRole);
    }

    @Override
    public boolean canUpdate(User user) {
        return isAdminOrAdminOfOrganization(user);
    }

    private boolean isAdminOrAdminOfOrganization(User user) {
        return user.hasRole(ADMIN)
                || user.hasRole(ORG_ADMIN) && user.getOrganizationId().equals(organizationId);
    }
}
