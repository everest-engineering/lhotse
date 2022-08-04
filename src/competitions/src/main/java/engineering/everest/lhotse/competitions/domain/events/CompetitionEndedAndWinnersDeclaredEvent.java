package engineering.everest.lhotse.competitions.domain.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;
import org.springframework.data.util.Pair;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Revision("0")
public class CompetitionEndedAndWinnersDeclaredEvent extends CompetitionEndedEvent {
    private List<Pair<UUID, UUID>> winnersToPhotoIdList;
    private int numVotesReceived;

    public CompetitionEndedAndWinnersDeclaredEvent(UUID competitionId, List<Pair<UUID, UUID>> winnersToPhotoIdList, int numVotesReceived) {
        super(competitionId);
        this.winnersToPhotoIdList = winnersToPhotoIdList;
        this.numVotesReceived = numVotesReceived;
    }
}
