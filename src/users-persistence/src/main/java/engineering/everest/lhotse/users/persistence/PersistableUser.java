package engineering.everest.lhotse.users.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "users")
public class PersistableUser {

    @Id
    private UUID id;
    private UUID organizationId;

    @Column(name = "username", unique = true)
    private String username;

    private String displayName;

    @Column(name = "email", unique = true)
    private String email;

    private boolean disabled;
    private Instant createdOn;
    private UUID profilePhotoFileId;

    public PersistableUser(UUID id, UUID organizationId, String username, String displayName, boolean disabled,
                           Instant createdOn) {
        this.id = id;
        this.organizationId = organizationId;
        this.username = username;
        this.displayName = displayName;
        this.disabled = disabled;
        this.createdOn = createdOn;

        this.email = this.username;
    }

    PersistableUser(UUID id, UUID organizationId, String displayName, String email, Instant createdOn) {
        this(id, organizationId, email, displayName, false, createdOn);
    }
}
