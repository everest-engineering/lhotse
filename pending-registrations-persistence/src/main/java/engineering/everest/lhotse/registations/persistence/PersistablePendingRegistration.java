package engineering.everest.lhotse.registations.persistence;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@Entity(name = "pendingregistrations")
public class PersistablePendingRegistration {

    @Id
    private UUID confirmationCode;
    private UUID organizationId;
    private UUID userId;
    private String userEmail;
    private Instant registeredOn;

    public PersistablePendingRegistration(UUID confirmationCode, UUID organizationId, UUID userId, String userEmail,
                                          Instant registeredOn) {
        this.confirmationCode = confirmationCode;
        this.organizationId = organizationId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.registeredOn = registeredOn;
    }
}
