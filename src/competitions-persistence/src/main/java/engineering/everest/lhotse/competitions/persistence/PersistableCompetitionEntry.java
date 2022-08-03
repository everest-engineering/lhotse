package engineering.everest.lhotse.competitions.persistence;

import engineering.everest.lhotse.competitions.domain.CompetitionEntry;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "competition_entries")
@IdClass(CompetitionEntryId.class)
public class PersistableCompetitionEntry {
    @Id
    private UUID competitionId;
    @Id
    private UUID photoId;
    private UUID submitterUserId;
    private Instant entryTimestamp;
    private int votesReceived;

    public CompetitionEntry toDomain() {
        return new CompetitionEntry(competitionId, photoId, submitterUserId, entryTimestamp, votesReceived);
    }
}
