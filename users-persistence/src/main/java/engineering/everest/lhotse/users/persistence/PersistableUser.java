package engineering.everest.lhotse.users.persistence;

import engineering.everest.lhotse.axon.common.domain.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.EnumSet;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class PersistableUser {

    private static final EnumSet<Role> DEFAULT_ROLES = EnumSet.of(Role.ORG_USER);

    @Id
    private UUID id;
    private UUID organizationId;

    @Indexed(unique = true)
    private String username;

    private String encodedPassword;
    private String displayName;

    @Indexed(unique = true)
    private String email;

    private boolean disabled;
    private EnumSet<Role> roles = EnumSet.noneOf(Role.class);
    private Instant createdOn;
    private UUID profilePhotoFileId;

    public PersistableUser(UUID id, UUID organizationId, String username, String encodedPassword, String displayName,
                           boolean disabled, EnumSet<Role> roles, Instant createdOn) {
        this.id = id;
        this.organizationId = organizationId;
        this.username = username;
        this.encodedPassword = encodedPassword;
        this.displayName = displayName;
        this.disabled = disabled;
        this.roles = roles;
        this.createdOn = createdOn;

        this.email = this.username;
    }

    PersistableUser(UUID id, UUID organizationId, String displayName, String email, String encodedPassword,
                    Instant createdOn) {
        this(id, organizationId, email, encodedPassword, displayName, false, createdOn);
    }

    PersistableUser(UUID id, UUID organizationId, String username, String encodedPassword, String displayName,
                    boolean disabled, Instant createdOn) {
        this(id, organizationId, username, encodedPassword, displayName, disabled,
                DEFAULT_ROLES, createdOn);
    }

    public void addRole(Role role) {
        roles.add(role);
    }

    public void removeRole(Role role) {
        roles.remove(role);
    }
}
