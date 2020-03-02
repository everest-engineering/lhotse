package engineering.everest.lhotse.axon.replay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.serialization.Revision;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Revision("0")
public class ReplayMarkerEvent {
    private UUID id;
}
