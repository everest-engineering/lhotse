package engineering.everest.lhotse.competitions.domain.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class PhotoEntryReceivedVoteEvent {
    private UUID competitionId;
    private UUID photoId;
    private UUID votingUserId;
}
