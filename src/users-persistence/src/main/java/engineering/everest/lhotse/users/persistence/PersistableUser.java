package engineering.everest.lhotse.users.persistence;

import engineering.everest.lhotse.axon.common.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

import static javax.persistence.FetchType.EAGER;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class PersistableUser {

    private static final Set<Role> DEFAULT_ROLES = EnumSet.of(Role.ORG_USER);

    @Id
    private UUID id;
    private UUID organizationId;

    @Column(name = "username", unique = true)
    private String username;

    private String displayName;

    @Column(name = "email", unique = true)
    private String email;

    private boolean disabled;
    @ElementCollection(fetch = EAGER)
    private Set<Role> roles = EnumSet.noneOf(Role.class);
    private Instant createdOn;
    private UUID profilePhotoFileId;

    public PersistableUser(UUID id, UUID organizationId, String username, String displayName,
                           boolean disabled, Set<Role> roles, Instant createdOn) {
        this.id = id;
        this.organizationId = organizationId;
        this.username = username;
        this.displayName = displayName;
        this.disabled = disabled;
        this.roles = roles;
        this.createdOn = createdOn;

        this.email = this.username;
    }

    PersistableUser(UUID id, UUID organizationId, String displayName, String email, Instant createdOn) {
        this(id, organizationId, email, displayName, false, createdOn);
    }

    PersistableUser(UUID id, UUID organizationId, String username, String displayName,
                    boolean disabled, Instant createdOn) {
        this(id, organizationId, username, displayName, disabled, DEFAULT_ROLES, createdOn);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }
}
