package engineering.everest.lhotse.api.rest.responses;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionEntryFragment {
    private UUID photoId;
    private UUID submitterUserId;
    private Instant entryTimestamp;
    private int votesReceived;
    private boolean didWin;
}
