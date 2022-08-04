package engineering.everest.lhotse.competitions.domain.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Revision("0")
public class CompetitionEndedAndWinnersDeclaredEvent extends CompetitionEndedEvent {
    private List<WinnerAndSubmittedPhotoPair> winnersToPhotoIdList;
    private int numVotesReceived;

    public CompetitionEndedAndWinnersDeclaredEvent(UUID competitionId,
                                                   List<WinnerAndSubmittedPhotoPair> winnersToPhotoIdList,
                                                   int numVotesReceived) {
        super(competitionId);
        this.winnersToPhotoIdList = winnersToPhotoIdList;
        this.numVotesReceived = numVotesReceived;
    }

}
