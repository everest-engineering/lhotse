package engineering.everest.lhotse.competitions.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CompetitionWithEntries extends Competition {
    private List<CompetitionEntry> entries;

    public CompetitionWithEntries(Competition competition, List<CompetitionEntry> entries) {
        super(competition.getId(), competition.getDescription(), competition.getSubmissionsOpenTimestamp(),
            competition.getSubmissionsCloseTimestamp(), competition.getVotingEndsTimestamp(),
            competition.getMaxEntriesPerUser());
        this.entries = entries;
    }
}
