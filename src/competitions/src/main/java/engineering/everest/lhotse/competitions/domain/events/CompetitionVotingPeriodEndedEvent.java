package engineering.everest.lhotse.competitions.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class CompetitionVotingPeriodEndedEvent {
    private UUID competitionId;
    private Instant scheduledVotingEndTimestamp;
}
