package engineering.everest.lhotse.registrations.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "pendingregistrations")

public class PersistablePendingRegistration {

    @Id
    private UUID confirmationCode;
    private UUID organizationId;
    private UUID userId;
    private String userEmail;
    private Instant registeredOn;
}
