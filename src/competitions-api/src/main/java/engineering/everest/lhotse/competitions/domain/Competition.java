package engineering.everest.lhotse.competitions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Competition {
    private UUID id;
    private String description;
    private Instant submissionsOpenTimestamp;
    private Instant submissionsCloseTimestamp;
    private Instant votingEndsTimestamp;
    private int maxEntriesPerUser;
}
