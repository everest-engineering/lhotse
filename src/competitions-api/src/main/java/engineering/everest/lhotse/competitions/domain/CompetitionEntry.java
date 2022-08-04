package engineering.everest.lhotse.competitions.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionEntry {
    private UUID competitionId;
    private UUID photoId;
    private UUID submittedByUserId;
    private Instant entryTimestamp;
    private int numVotesReceived;
    private boolean isWinner;
}
