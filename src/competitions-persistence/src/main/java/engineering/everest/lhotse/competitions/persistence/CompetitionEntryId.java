package engineering.everest.lhotse.competitions.persistence;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionEntryId implements Serializable {
    private UUID competitionId;
    private UUID photoId;
}
