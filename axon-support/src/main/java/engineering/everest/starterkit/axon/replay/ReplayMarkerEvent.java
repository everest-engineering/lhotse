package engineering.everest.starterkit.axon.replay;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReplayMarkerEvent {
    private UUID id;
}
