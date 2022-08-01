package engineering.everest.lhotse.competitions.persistence;

import engineering.everest.lhotse.competitions.domain.Competition;
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
@Entity(name = "competitions")
public class PersistableCompetition {
    @Id
    private UUID id;
    private String description;
    private Instant submissionsOpenTimestamp;
    private Instant submissionsCloseTimestamp;
    private Instant votingEndsTimestamp;
    private int maxEntriesPerUser;

    public Competition toDomain() {
        return new Competition(id, description, submissionsOpenTimestamp, submissionsCloseTimestamp,
            votingEndsTimestamp, maxEntriesPerUser);
    }
}
