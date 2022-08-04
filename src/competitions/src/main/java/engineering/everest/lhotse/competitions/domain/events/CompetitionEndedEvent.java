package engineering.everest.lhotse.competitions.domain.events;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Revision("0")
public class CompetitionEndedEvent {
    UUID competitionId;
}
