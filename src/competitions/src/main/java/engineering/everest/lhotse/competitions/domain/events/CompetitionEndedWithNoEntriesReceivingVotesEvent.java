package engineering.everest.lhotse.competitions.domain.events;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@Getter
@EqualsAndHashCode(callSuper = true)
@Revision("0")
public class CompetitionEndedWithNoEntriesReceivingVotesEvent extends CompetitionEndedEvent {
    private int numberOfEntriesReceived;

    public CompetitionEndedWithNoEntriesReceivingVotesEvent(UUID competitionId) {
        super(competitionId);
    }
}
