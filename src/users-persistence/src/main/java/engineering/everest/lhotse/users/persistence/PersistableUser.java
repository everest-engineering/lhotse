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
    private String displayName;
    @Column(name = "emailaddress", unique = true)
    private String emailAddress;

    private boolean disabled;
    private Instant createdOn;
    private UUID profilePhotoFileId;

    public PersistableUser(UUID id,
                           UUID organizationId,
                           String displayName,
                           String emailAddress,
                           boolean disabled,
                           Instant createdOn) {
        this.id = id;
        this.organizationId = organizationId;
        this.displayName = displayName;
        this.emailAddress = emailAddress;
        this.disabled = disabled;
        this.createdOn = createdOn;
    }

    PersistableUser(UUID id, UUID organizationId, String displayName, String email, Instant createdOn) {
        this(id, organizationId, displayName, email, false, createdOn);
    }
}
