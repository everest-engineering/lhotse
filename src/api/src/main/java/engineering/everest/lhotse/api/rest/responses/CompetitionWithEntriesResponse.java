package engineering.everest.lhotse.api.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionWithEntriesResponse {
    private UUID id;
    private String description;
    private Instant submissionsOpenTimestamp;
    private Instant submissionsCloseTimestamp;
    private Instant votingEndsTimestamp;
    private int maxEntriesPerUser;
    private List<CompetitionEntryFragment> entries;
}
