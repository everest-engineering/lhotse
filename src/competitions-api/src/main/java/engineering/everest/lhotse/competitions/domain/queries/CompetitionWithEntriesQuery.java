package engineering.everest.lhotse.competitions.domain.queries;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CompetitionWithEntriesQuery {
    private UUID competitionId;
}
