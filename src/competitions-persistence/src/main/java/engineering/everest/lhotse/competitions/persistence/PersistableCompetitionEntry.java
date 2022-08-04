package engineering.everest.lhotse.competitions.persistence;

import engineering.everest.lhotse.competitions.domain.CompetitionEntry;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
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
    private boolean isWinner;

    public PersistableCompetitionEntry(UUID competitionId, UUID photoId, UUID submitterUserId, Instant entryTimestamp) {
        this.competitionId = competitionId;
        this.photoId = photoId;
        this.submitterUserId = submitterUserId;
        this.entryTimestamp = entryTimestamp;
        this.votesReceived = 0;
        this.isWinner = false;
    }

    public CompetitionEntry toDomain() {
        return new CompetitionEntry(competitionId, photoId, submitterUserId, entryTimestamp, votesReceived, isWinner);
    }
}
